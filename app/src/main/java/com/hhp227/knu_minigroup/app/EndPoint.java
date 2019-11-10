package com.hhp227.knu_minigroup.app;

public interface EndPoint {
    String BASE_URL = "http://lms.knu.ac.kr";
    String LOGIN = BASE_URL + "/ilos/lo/login.acl";
    String GROUP_LIST = BASE_URL + "/ilos/m/community/share_group_list.acl";
    String CREATE_GROUP = BASE_URL + "/ilos/community/share_group_insert.acl";
    String GROUP_IMAGE_UPDATE = BASE_URL + "/ilos/community/share_group_image_update.acl";
    String GROUP_ARTICLE_LIST = BASE_URL + "/ilos/community/share_list.acl";
    String WRITE_ARTICLE = BASE_URL + "/ilos/community/share_insert.acl";
    String IMAGE_UPLOAD = BASE_URL + "/ilos/tinymce/file_upload_pop.acl";
    String DELETE_ARTICLE = BASE_URL + "/ilos/community/share_delete.acl";

    String URL_KNU = "http://www.knu.ac.kr";
    String URL_KNU_NOTICE = URL_KNU + "/wbbs/wbbs/bbs/btin/list.action?bbs_cde=1&btin.page={PAGE}&popupDeco=false&btin.search_type=&btin.search_text=&menu_idx=67";
    String URL_KNULIBRARY_SEAT = "http://seat.knu.ac.kr/smufu-api/pc/{ID}/rooms-at-seat";
}
