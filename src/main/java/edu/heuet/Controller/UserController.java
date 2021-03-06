package edu.heuet.Controller;

import edu.heuet.Pojo.UserInfo;
import edu.heuet.Service.UserService;
import edu.heuet.Util.CheckCode;
import edu.heuet.Util.JuheDemo;
import org.json.CookieList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller("userController")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;//注入业务逻辑层接口
     //注册  的controller方法
    @RequestMapping("/register")
    public String register(UserInfo userInfo,String CheckCode, Model model,HttpServletRequest request){
        Cookie[] coderegister=request.getCookies();
        String value="";
        for(Cookie cookie:coderegister){
            if(cookie.getName().equals("coderegister")){
                value=cookie.getValue();
            }
        }
        String checkCode=CheckCode+"/"+userInfo.getPhoneNum();
        checkCode=DigestUtils.md5DigestAsHex(checkCode.getBytes());
        if(checkCode.equals(value)){
            if(userService.registerInfo(userInfo)){
                return "index/login";
            }
            else {
                return "index/regist";
            }
        }else{
            return "index/regist";
        }

    }
 /***   手机号检验  */
    @RequestMapping("/CheckPNum")
    @ResponseBody
    public boolean CheckPNum(String PhoneNum){
        boolean s=userService.checkPNum(PhoneNum);
        return s;
    }

    /**退出登录**/
    @RequestMapping("/unlogin")
    public String Unlogin(HttpSession session){
        session.invalidate();
        return "/index/index";
    }


//     登录控制
    @RequestMapping("/login")
    public String login(UserInfo userInfo , @RequestParam("CheckCode")String  CheckCode, HttpSession session, Model model, HttpServletRequest request){
            // 将信息传给业务逻辑层
        System.out.println(userInfo);
        UserInfo u=userService.selectUserInfo(userInfo);
        System.out.println(u);
        if(u!=null){
            //跳转到首页
            if(u.getCredit()<60){
                return "index/limit_login";
            }else {
                if(userInfo.getLPassword()!=null){
                    session.setAttribute("UserName", u.getUserName());
                    session.setAttribute("UserId", u.getUserId());
                    session.setAttribute("user",u);
                    return "index/index";
                }else {
                    Cookie[] codelogin=request.getCookies();
                    String value="";
//                    System.out.println(codelogin);
                    for(Cookie cookie:codelogin){
                        if(cookie.getName().equals("codelogin")){
                            System.out.println(cookie.getValue());
                            value=cookie.getValue();
                        }
                    }
                    String checkCode=CheckCode+"/"+userInfo.getPhoneNum();
                    checkCode=DigestUtils.md5DigestAsHex(checkCode.getBytes());
                    if(checkCode.equals(value)) {
                        session.setAttribute("UserName", u.getUserName());
                        session.setAttribute("UserId", u.getUserId());
                        session.setAttribute("user",u);
                        return "index/index";
                    }else {
                        System.out.println("22222222222222222");
                        return "index/failed";
                    }
                }
            }


        }else {
            //跳转到注册页面
            System.out.println("111111111111");
            return "index/failed";
        }

    }
    @RequestMapping("/check")   /** 核对验证码**/
    public @ResponseBody  String check(@RequestParam("PhoneNum") String PhoneNum , @RequestParam("goal") int goal , Model model, HttpServletResponse response){
                if(goal==0){
                    String codelogin=""+new CheckCode().CheckCode1();
                    String base=codelogin+"/"+PhoneNum;
                    String md5Code = DigestUtils.md5DigestAsHex(base.getBytes());
                    Cookie cookie=new Cookie("codelogin",md5Code);
//                    cookie.setMaxAge(1800);
                    response.addCookie(cookie);
                    System.out.println(codelogin);
                }else if(goal==1){
                    String coderegister=""+new CheckCode().CheckCode1();
                    String base=coderegister+"/"+PhoneNum;
                    String md5Code = DigestUtils.md5DigestAsHex(base.getBytes());
                    Cookie cookie=new Cookie("coderegister",md5Code);
                    cookie.setMaxAge(1800);
                    response.addCookie(cookie);
                    System.out.println(coderegister);
                }


//            String s= JuheDemo.mobileQuery(PhoneNum,code);

            return "true";//返回字符串true
    }


    @RequestMapping("/alterInfo")   /**修改个人资料**/
    public String AlterInfo(UserInfo userInfo,Model model,HttpSession session){
        UserInfo userInfo1=(UserInfo) session.getAttribute("user");
        String userid=session.getAttribute("UserId").toString();
        userInfo.setUserId(userid);
        boolean i=userService.updateInfo(userInfo);
        if(i) {
            session.setAttribute("UserName", userInfo.getUserName());
            userInfo1.setPhoneNum(userInfo.getPhoneNum());
            userInfo1.setSchool(userInfo.getSchool());
            userInfo1.setUserName(userInfo.getUserName());
            session.setAttribute("user", userInfo1);
            model.addAttribute("flag",3);
            return "PersonCenter/success";
        }else {
            model.addAttribute("flag",4);
            return "PersonCenter/infor";
        }

    }
    @RequestMapping("/alterPwd")  /*** 修改密码**/
    public String AlterPwd(@RequestParam("LPassword") String LPassword,@RequestParam("NewLPassword")String NewLPassword ,Model model,HttpSession session){
        int i=userService.updatePwd(LPassword,NewLPassword,session.getAttribute("UserId").toString());
        return "index/login";
    }


}
