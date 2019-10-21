package com.hhp227.knu_minigroup.app;

public interface EndPoint {
    String BASE_URL = "http://lms.knu.ac.kr/";
    String LOGIN = BASE_URL + "ilos/lo/login.acl";
    String GROUP_LIST = BASE_URL + "ilos/main/share_list.acl";
    String CREATE_GROUP = BASE_URL + "ilos/community/share_group_insert.acl";
}
