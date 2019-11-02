package com.hhp227.knu_minigroup.app;

public interface EndPoint {
    String BASE_URL = "http://lms.knu.ac.kr/";
    String LOGIN = BASE_URL + "ilos/lo/login.acl";
    String GROUP_LIST = BASE_URL + "ilos/m/community/share_group_list.acl";
    String CREATE_GROUP = BASE_URL + "ilos/community/share_group_insert.acl";
    String GROUP_IMAGE_UPDATE = BASE_URL + "ilos/community/share_group_image_update.acl";
    String GROUP_FEED_LIST = BASE_URL + "ilos/community/share_list.acl";
}
