package com.cheng.weixin.web.mobile.controller;

import com.cheng.weixin.web.mobile.model.Response;
import com.cheng.weixin.web.mobile.param.ProductDto;
import com.cheng.weixin.web.mobile.security.IgnoreSecurity;
import com.cheng.weixin.web.mobile.service.SysProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Desc: 商品
 * Author: hp
 * Date: 2016/10/8
 */
@RestController
@RequestMapping("product")
public class ProductController extends BaseController {
    @Autowired
    private SysProductService productService;

    /** 获取商品详情 **/
    @IgnoreSecurity
    @RequestMapping(value = "v1/detail")
    public ResponseEntity<Response> sendMsgCode(HttpServletRequest request) {
        ProductDto product = (ProductDto) getDto(request, ProductDto.class);
        return success(productService.getDetail(product.getProductId()));
    }

}
