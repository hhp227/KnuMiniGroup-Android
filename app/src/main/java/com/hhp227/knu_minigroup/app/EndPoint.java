package com.hhp227.knu_minigroup.app;

public interface EndPoint {

    // 경북대 LMS URL
    String BASE_URL = "http://lms.knu.ac.kr";
    String LOGIN = BASE_URL + "/ilos/lo/login.acl";
    String GROUP_LIST = BASE_URL + "/ilos/m/community/share_group_list.acl";
    String CREATE_GROUP = BASE_URL + "/ilos/community/share_group_insert.acl";
    String REGISTER_GROUP = BASE_URL + "/ilos/community/share_group_register.acl";
    String WITHDRAWAL_GROUP = BASE_URL + "/ilos/community/share_auth_drop_me.acl";
    String MODIFY_GROUP = BASE_URL + "/ilos/community/share_group_modify.acl";
    String UPDATE_GROUP = BASE_URL + "/ilos/community/share_group_update.acl";
    String DELETE_GROUP = BASE_URL + "/ilos/community/share_group_delete.acl";
    String GROUP_MEMBER_LIST = BASE_URL + "/ilos/community/share_group_member_list.acl";
    String GROUP_IMAGE_UPDATE = BASE_URL + "/ilos/community/share_group_image_update.acl";
    String GROUP_ARTICLE_LIST = BASE_URL + "/ilos/community/share_list.acl";
    String WRITE_ARTICLE = BASE_URL + "/ilos/community/share_insert.acl";
    String IMAGE_UPLOAD = BASE_URL + "/ilos/tinymce/file_upload_pop.acl";
    String DELETE_ARTICLE = BASE_URL + "/ilos/community/share_delete.acl";
    String MODIFY_ARTICLE = BASE_URL + "/ilos/community/share_update.acl";
    String INSERT_REPLY = BASE_URL + "/ilos/community/share_comment_insert.acl";
    String DELETE_REPLY = BASE_URL + "/ilos/community/share_comment_delete.acl";
    String MODIFY_REPLY = BASE_URL + "/ilos/community/share_comment_update.acl";
    String MEMBER_LIST = BASE_URL + "/ilos/community/share_member_list.acl";
    String USER_IMAGE = BASE_URL + "/ilos/mp/user_image_view.acl?id={UID}&ext=.jpg";
    String GET_USER_IMAGE = BASE_URL + "/ilos/mp/myinfo_update_photo.acl";
    String TIMETABLE = BASE_URL + "/ilos/st/main/pop_academic_timetable_form.acl";
    String NEW_MESSAGE = BASE_URL + "/ilos/message/received_new_message_check.acl";
    String SEND_MESSAGE = BASE_URL + "/ilos/co/club_send_msg_insert.acl";
    String GROUP_IMAGE = BASE_URL + "/ilosfiles2/club/photo/{FILE}";
    String DEFAULT_GROUP_IMAGE = BASE_URL + "/ilos/images/community/share_nophoto.gif";

    // 로그기록
    String CREATE_LOG = "http://knu.dothome.co.kr/knu/v1/register";

    // 학교 URL
    String URL_KNU = "http://www.knu.ac.kr";
    String URL_SCHEDULE = URL_KNU + "/wbbs/wbbs/user/yearSchedule/xmlResponse.action?schedule.search_date={YEAR-MONTH}";
    String URL_SHUTTLE = URL_KNU + "/wbbs/wbbs/contents/index.action?menu_url=intro/{SHUTTLE}&menu_idx=27";
    String URL_KNU_NOTICE = URL_KNU + "/wbbs/wbbs/bbs/btin/list.action?bbs_cde=1&btin.page={PAGE}&popupDeco=false&btin.search_type=&btin.search_text=&menu_idx=67";
    String URL_KNULIBRARY_SEAT = "http://seat.knu.ac.kr/smufu-api/pc/{ID}/rooms-at-seat";
    String URL_KNU_DORM_MEAL = "http://dorm.knu.ac.kr/xml/food.php?get_mode={ID}";
    String URL_KNU_SC_DORM_MEAL = "http://dorm.knu.ac.kr/scdorm/_new_ver/";

    // 외부 URL
    String URL_INTER_CITY_SHUTTLE = "http://www.gobus.co.kr/north/inquiry/inquiry_see.asp?code=300&explice=경북대상주";
}
