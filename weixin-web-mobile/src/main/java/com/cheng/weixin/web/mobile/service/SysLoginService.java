package com.cheng.weixin.web.mobile.service;

import com.cheng.weixin.common.utils.ServletUtils;
import com.cheng.weixin.common.utils.SystemUtils;
import com.cheng.weixin.rabbitmq.enums.MsgType;
import com.cheng.weixin.rabbitmq.model.SmsModel;
import com.cheng.weixin.rpc.message.entity.SmsHistory;
import com.cheng.weixin.rpc.message.service.RpcSmsService;
import com.cheng.weixin.rpc.rabbitmq.service.RpcRabbitSmsService;
import com.cheng.weixin.rpc.user.entity.Account;
import com.cheng.weixin.rpc.user.service.RpcUserService;
import com.cheng.weixin.web.mobile.exception.BusinessException;
import com.cheng.weixin.web.mobile.exception.LoginException;
import com.cheng.weixin.web.mobile.exception.UserException;
import com.cheng.weixin.web.mobile.exception.message.StatusCode;
import com.cheng.weixin.web.mobile.param.LoginDto;
import com.cheng.weixin.web.mobile.param.RegDto;
import com.cheng.weixin.web.mobile.security.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 登录
 * Author: cheng
 * Date: 2016/7/8
 */
@Service("sysLoginService")
public class SysLoginService {

    @Autowired
    private RpcRabbitSmsService rabbitService;
    @Autowired
    private RpcSmsService smsService;
    @Autowired
    private RpcUserService userService;
    @Autowired
    private TokenManager tokenManager;

    /**
     * 发送验证码
     * @param phone 登陆账号
     */
    public void sendRegMsgCode(String phone) {

        if (checkAccountIsExistByLoginName(phone)) {
            throw new UserException(StatusCode.USER_EXIST);
        }

        String userIp = SystemUtils.getRemoteAddr(ServletUtils.getRequest());
        int countByDay = smsService.getCountByDay(phone);
        if (countByDay >= 4) {
            throw new BusinessException("当前手机号"+phone+"发送次数太多");
        }
        int countByIp = smsService.getCountByIp(userIp);
        if (countByIp >= 4) {
            throw new BusinessException("当前IP"+userIp+"发送次数太多");
        }

        SmsModel smsModel = new SmsModel();
        smsModel.setUserIp(userIp);
        smsModel.setPhone(phone);
        rabbitService.sendValidate(smsModel);
    }

    /**
     * 验证验证码
     * @param phone
     * @param code
     */
    public boolean checkCode(String phone, String code) {
        SmsHistory smsHistory = smsService.getInfoByPhoneAndType(phone, MsgType.VALIDATE);
        if (smsHistory == null) {
            throw new LoginException(StatusCode.PHONE_NOT_EXIST);
        }
        return code.equals(smsHistory.getValidate());
    }

    /**
     * 保存注册信息
     * @param regDto
     * @return
     */
    public String saveAccess(RegDto regDto) {
        if (!checkCode(regDto.getPhone(), regDto.getValidate())) {
            throw new LoginException(StatusCode.USER_VALIDATE_ERROR);
        }
        String userIp = SystemUtils.getRemoteAddr(ServletUtils.getRequest());
        userService.saveAccess(regDto.getPhone(), regDto.getPassword(), regDto.getNickname(), userIp);
        return tokenManager.createToken(regDto.getPhone());

    }
    /**
     * 用户登录
     * @param loginDto
     */
    public String login(LoginDto loginDto) {
        String loginIp = SystemUtils.getRemoteAddr(ServletUtils.getRequest());
        String result = userService.validateLogin(loginDto.getUsername(), loginDto.getPassword(), loginIp);
        if ("PASSWDFAIL".equals(result) || "NOTUSER".equals(result)) {
            throw new LoginException(StatusCode.LOGIN_FAIL);
        }
        return tokenManager.createToken(loginDto.getUsername());
    }

    /**
     * 检查该用户是否存在
     * @param LoginName 登录名
     */
    private boolean checkAccountIsExistByLoginName(String LoginName) {
        Account account = userService.getAccountByLoginName(LoginName);
        return null != account;
    }
}
