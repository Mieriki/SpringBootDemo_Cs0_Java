package com.example.service.impl;

import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import com.example.utils.HostHolder;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl implements AccountService {
    @Resource
    AccountMapper mapper;

    @Resource
    HostHolder hostHolder;

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils utils;

    @Resource
    PasswordEncoder encoder;

    @Value("${spring.mail.verify.char-set}")
    String verifyCharSet;

    @Value("${spring.mail.verify.timeout}")
    Integer timeout;

    @Value("${spring.mail.verify.limit}")
    Integer limit;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByUsernameOrEmail(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        hostHolder.setAccount(account);
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account findAccountByUsernameOrEmail(String info) {
        return mapper.findAccountByUsernameOrEmail(info);
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String address) {
        if (!type.equals("register") && !existxAccountByEmail(email)) return "该邮箱还未被注册";
        synchronized (address.intern()) {
            if (!this.verifyLimit(address)) return "请求过于频繁，请稍后再试";
            String code = this.getVerifyCode();
            Map<String, String> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email + type, code, timeout, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String key = getVerifyRegisterKey(email);
        String code = stringRedisTemplate.opsForValue().get(key);
        String username = vo.getUsername();
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误, 请重新输入";
        if (existxAccountByEmail(email)) return "该邮箱已被注册";
        if (existxAccountByUsername(username)) return "该用户名已被注册";
        String password = encoder.encode(vo.getPassword());
        Account account = Account.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(Const.ROLE_USER)
                .registerTime(new Date())
                .build();
        if (mapper.addAccount(account)) {
            stringRedisTemplate.delete(key);
            return null;
        } else {
            return "内部错误,请联系管理员";
        }
    }

    @Override
    public boolean existxAccountByEmail(String email) {
        return mapper.existxAccountByEmail(email);
    }

    @Override
    public boolean existxAccountByUsername(String username) {
        return mapper.existxAccountByUsername(username);
    }

    @Override
    public boolean resetPasswordByEmail(Account account) {
        return mapper.resetPasswordByEmail(account);
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String key = getVerifyResetKey(email);
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误, 请重新输入";
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForValue()
                .set(getResetPasswordKey(email), "", timeout, TimeUnit.MINUTES);
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String key = getResetPasswordKey(email);
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) return "请先进行邮箱验证";
        Account account = Account.builder()
                .email(email)
                .password(encoder.encode(vo.getPassword()))
                .build();
        if (!resetPasswordByEmail(account)) return "内部错误,请联系管理员";
        stringRedisTemplate.delete(key);
        return null;
    }

    public String getVerifyCode() {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(verifyCharSet.charAt(random.nextInt(verifyCharSet.length())));
        }
        return stringBuilder.toString();
    }

    public boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return utils.limitOnceCheck(key, limit);
    }

    private String getVerifyRegisterKey(String email) {
        return Const.VERIFY_EMAIL_DATA + email + Const.VERIFY_TYPE_RESGITER;
    }

    private String getVerifyResetKey(String email) {
        return Const.VERIFY_EMAIL_DATA + email + Const.VERIFY_TYPE_RESET;
    }

    private String getResetPasswordKey(String email) {
        return Const.RESET_PASSWORD_EMAIL + email;
    }
}
