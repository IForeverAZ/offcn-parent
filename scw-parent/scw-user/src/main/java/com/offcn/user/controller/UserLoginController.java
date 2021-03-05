package com.offcn.user.controller;

import com.netflix.discovery.converters.Auto;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.user.componet.SmsTemplate;
import com.offcn.user.po.TMember;
import com.offcn.user.service.UserService;
import com.offcn.user.vo.req.UserRegistVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api(tags = "用户登录/注册")
@Slf4j
public class UserLoginController {
    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;

    @ApiOperation(value="获取短信验证码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "手机号码",name = "phoneNo",required = true)
    })
    @GetMapping("/sendCode")
    public AppResponse<Object> sendCode(String phoneNo){
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 6);
        stringRedisTemplate.opsForValue().set(phoneNo,code,60*60*24*7, TimeUnit.SECONDS);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile",phoneNo);
        querys.put("param","code:"+code);
        querys.put("tpl_id","TP1711063");
        //发短信
        String s = smsTemplate.sendCode(querys);
        if(s.equals("fail")){
            return AppResponse.fail("短信发送失败");
        }
        return AppResponse.ok(s);

    }

    @ApiOperation("用户注册")
    @PostMapping("/regist")
    public AppResponse<Object> regist(UserRegistVo registVo) {
        //1、校验验证码
        String code = stringRedisTemplate.opsForValue().get(registVo.getLoginacct());
        if (!StringUtils.isEmpty(code)) {
            //redis中有验证码
            boolean b = code.equalsIgnoreCase(registVo.getCode());
            if (b) {
                //2、将vo转业务能用的数据对象
                TMember member = new TMember();
                BeanUtils.copyProperties(registVo, member);
                //3、将用户信息注册到数据库
                try {
                    userService.registUser(member);
                    log.debug("用户信息注册成功：{}", member.getLoginacct());
                    //4、注册成功后，删除验证码
                    stringRedisTemplate.delete(registVo.getLoginacct());
                    return AppResponse.ok("注册成功...");
                } catch (Exception e) {
                    log.error("用户信息注册失败：{}", member.getLoginacct());
                    return AppResponse.fail(e.getMessage());
                }
            } else {
                return AppResponse.fail("验证码错误");
            }
        } else {
            return AppResponse.fail("验证码过期，请重新获取");
        }
    }
}
