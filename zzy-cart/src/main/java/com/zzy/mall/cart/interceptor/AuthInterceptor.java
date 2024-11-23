package com.zzy.mall.cart.interceptor;

import com.alibaba.fastjson.JSON;
import com.zzy.mall.common.constant.AuthConstant;
import com.zzy.mall.common.vo.MemberVO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 我们自定义的拦截器: 帮助我们获取当前登录的用户信息
 * 通过Session共享获取的
 */
public class AuthInterceptor implements HandlerInterceptor {

    // 本地线程对象 Map<Thread,Object>
    public static ThreadLocal<MemberVO> threadLocal = new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 通过HttpSession获取当前登录的用户信息
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(AuthConstant.AUTH_SESSION_REDIS);
        if (attribute != null) {
            MemberVO member = (MemberVO) attribute;
            threadLocal.set(member);  // 放置在本地线程中
            return true;
        }
        // 如果 attribute==null 说明没有登录，那么我们就需要重定向到登录页面
        session.setAttribute(AuthConstant.AUTH_SESSION_MSG,"请先登录");
        response.sendRedirect("http://auth.zzy.com/login.html");
        return false;
    }
}
