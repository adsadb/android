package com.example.homework;


import com.example.homework.utils.Base64Util;
import com.example.homework.utils.FileToByte;
import com.example.homework.utils.GsonUtils;
import com.example.homework.utils.HttpUtil;


import java.util.HashMap;
import java.util.Map;

public class FaceManage {
    public static String add(String image,String group_id,String user_id,String user_info) {
        // 请求url
        String ACCESS_TOKEN = "24.2b4e8521e170be3f70909cd1204d3ab2.2592000.1575802862.282335-17657005";
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", image);
            map.put("group_id", group_id);
            map.put("user_id", user_id);
            map.put("user_info", user_info);
            map.put("liveness_control", "NORMAL");
            map.put("image_type", "FACE_TOKEN");
            map.put("quality_control", "LOW");

            String param = GsonUtils.toJson(map);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
//            String accessToken = GetAuth.getAuth();

            String result = HttpUtil.post(url, ACCESS_TOKEN, "application/json", param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void test1(){
        String path = "C:\\Users\\沛源\\Downloads\\api.jpg";
        String image = Base64Util.encode(FileToByte.getBytesByFile(path));
        System.out.println(image);
        System.out.println(add(image,"1","1","123"));
    }
}
