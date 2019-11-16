package com.example.homework;

import com.example.homework.utils.GsonUtils;
import com.example.homework.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class FaceCompare {
    public static String faceSearch(String image,String group_id_list) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/search";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("image", image);
            map.put("liveness_control", "NORMAL");
            map.put("group_id_list", group_id_list);
            map.put("image_type", "FACE_TOKEN");
            map.put("quality_control", "LOW");

            String param = GsonUtils.toJson(map);

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "[调用鉴权接口获取的token]";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
