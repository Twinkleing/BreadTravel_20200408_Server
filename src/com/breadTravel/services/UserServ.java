package com.breadTravel.services;


import com.breadTravel.dao.UserDaoImpl;
import com.breadTravel.email.SendCode;
import com.breadTravel.entity.UserInfo;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@WebServlet(name = "UserServ", urlPatterns = "/UserServ")
public class UserServ extends HttpServlet {

    private static Map<String, String> codeMap = new HashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String str = req.getParameter("act");
        if (str.equals("login")) {
            login(req, resp);
        }
        if (str.equals("register")) {
            register(req, resp);
        }
        if (str.equals("checkEmail")) {
            checkEmail(req, resp);
        }
        if (str.equals("getCode")) {
            getCode(req, resp);
        }
        if (str.equals("verification")) {
            verification(req, resp);
        }
        if (str.equals("upLoadHeadImage")) {
            upLoadHeadImage(req, resp);
        }
        if (str.equals("getHeadImgFile")) {
            getImageFile(req, resp);
        }
        if (str.equals("getHeadImg")) {
            getHeadImg(req, resp);
        }
        if (str.equals("getNickName")){
            getNickName(req,resp);
        }
    }

    private void getNickName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = req.getParameter("userName");
        String nickName=new UserDaoImpl().querySelectNickName(userName);
        PrintWriter out=resp.getWriter();
        out.print(nickName);
    }

    private void getHeadImg(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = req.getParameter("userName");
        String headImg = new UserDaoImpl().querySelectHeadImg(userName);
        System.out.println("getHeadImg    " + userName + ":" + headImg);
        PrintWriter out = resp.getWriter();
        out.print(headImg);
    }

    private void getImageFile(HttpServletRequest req, HttpServletResponse resp) {
        String userName = req.getParameter("userName");
        String headImg = new UserDaoImpl().querySelectHeadImg(userName);
        System.out.println("getImageFile    " + userName + ":" + headImg);
        try {
            String filePath;
            if (headImg.equals("logo.jpg")) {
                filePath = this.getServletContext().getRealPath("/upload/logo.jpg");
            } else {

                filePath = this.getServletContext().getRealPath("/upload/" + userName + "/" + headImg);
            }
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = resp.getOutputStream();
            byte[] bytes = new byte[1024];
            int a = 0;
            while ((a = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, a);
            }
            System.out.println("pass");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }

    private void upLoadHeadImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        // ??????????????????????????????
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // ????????????????????????
        String savePath = this.getServletContext().getRealPath("/upload");
        // ????????????????????????????????????????????????????????????Tomcat???????????????temp?????????
        String temp = System.getProperty("java.io.tmpdir");
        // ???????????????????????? 5M
        factory.setSizeThreshold(1024 * 1024 * 5);
        // ????????????????????????temp
        factory.setRepository(new File(temp));
        // ??????????????????????????????,ServletFileUpload ??????????????????????????????
        ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
        String value = "", name = "";
        // ??????????????????List???
        try {
            //??????????????????????????????????????????????????????
            List<FileItem> list = servletFileUpload.parseRequest(request);  //FileItem

            //??????list???????????????????????????????????????????????????fileItem??????
            for (FileItem item : list) {

                if (item.isFormField()) {
                    //???????????????????????????
                    name = item.getFieldName();  //????????????????????????
                    value = item.getString();
                    savePath = savePath + "\\" + value;
                } else {
                    //?????????????????????
                    String filename = item.getName();  //?????????????????????  C:\Documents and Settings\ThinkPad\??????\1.txt
                    filename = filename.substring(filename.lastIndexOf("\\") + 1);
                    InputStream in = item.getInputStream();   //??????????????????
                    int len = 0;
                    byte buffer[] = new byte[1024];
                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(savePath + "\\" + filename);  //???upload?????????????????????
                    while ((len = in.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    System.out.println("uploadHeadImg   " + value + ":" + filename);
                    boolean flag = new UserDaoImpl().uploadHeadImg(value, filename);
                    if (flag == true) {
                        out.write("T");
                        System.out.println("pass");
                    } else {
                        out.write("F");
                        System.out.println("error");
                    }
                    in.close();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.write("F");
        }
        out.flush();
        out.close();

    }


    private void verification(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        String email = req.getParameter("email");
        System.out.println("verification    " + email + ":" + code);
        PrintWriter out = resp.getWriter();
        if (code.equals(codeMap.get(email))) {
            out.print("T");
        } else {
            out.print("F");
        }

    }

    private void timeLimit(String email) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                codeMap.remove(email);
            }
        }, 1 * 60 * 1000);

    }

    private void getCode(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String email = req.getParameter("email");
        System.out.println("getCode " + email);
        String code = new SendCode().Verification(req, resp, email);
        codeMap.put(email, code);
        timeLimit(email);
    }

    private void checkEmail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        System.out.println("checkEmail " + email);
        boolean flag = new UserDaoImpl().queryCheckEmail(email);
        PrintWriter out = resp.getWriter();
        if (flag != true) {
            out.print("T");
            System.out.println("pass");
        } else {
            out.print("F");
            System.out.println("error");
        }

    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = "";
        String passWord = req.getParameter("passWord");
        String email = req.getParameter("email");
        userName = email.substring(0, email.indexOf("@"));
        System.out.println("Register    " + userName + ":" + passWord + ":" + email);
        boolean flag = new UserDaoImpl().queryRegister(userName, passWord, email);
        File file = new File("G:\\IDEA_project\\BreadTravel_20200408\\out\\artifacts\\BreadTravel_20200408_war_exploded\\upload\\" + userName);
        if (!file.exists()) {
            file.mkdir();
        }
        PrintWriter out = resp.getWriter();
        if (flag == true) {
            List<UserInfo> list = new ArrayList<>();
            list.add(new UserInfo(userName, email));
            JSONArray jsonArray = new JSONArray();
            for (UserInfo userInfo : list) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", userInfo.getUserName());
                jsonObject.put("email", userInfo.getEmail());
                jsonArray.put(jsonObject);
            }
            out.write(jsonArray.toString());
            System.out.println("pass");
        } else {
            out.print("F");
            System.out.println("error");
        }

    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String account = req.getParameter("account");
        String passWord = req.getParameter("passWord");
        System.out.println("Login   " + account + ":" + passWord);
        List<UserInfo> list = new UserDaoImpl().queryLogin(account, passWord);
        PrintWriter out = resp.getWriter();
        if (list.size() != 0) {
            JSONArray jsonArray = new JSONArray();
            for (UserInfo userInfo : list) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userName", userInfo.getUserName());
                jsonObject.put("email", userInfo.getEmail());
                jsonArray.put(jsonObject);
            }
            out.write(jsonArray.toString());
            System.out.println("pass");
        } else {
            out.print("F");
            System.out.println("error");
        }
    }
}
