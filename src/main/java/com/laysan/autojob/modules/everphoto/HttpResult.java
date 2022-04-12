package com.laysan.autojob.modules.everphoto;

import lombok.Data;

/**
 * {
 * "timestamp": 1649766997,
 * "code": 0,
 * "data": {
 * "token": "OQ0T7CZtrbfQUHZqh8yfpKdS",
 * "user_profile": {
 * "id": 6462571666831574000,
 * "name": "依山慕雪",
 * "name_pinyin": [
 * "yi",
 * "shan",
 * "mu",
 * "xue"
 * ],
 * "country_code": "+86",
 * "avatar_fid": "50f12769",
 * "mobile": "177******95",
 * "mobile_local": "177******95",
 * "usage": 114858284050,
 * "secret_digit_enc": "aea08a462adf3750263eab15a438a8a9",
 * "secret_digit_enc_v2": "yh7nnWHZPzu4cSBbVtLaNr0+QQMVto0bsK8Zceu61YchscjdV8mPxQVUhyp/loSlY8QLcRp4GBa4mUd8mw+OqxbP3KBCivTS88V636vQZtsbMHCmwVC1dvX8uPJv3uIzfeZv6cgswZ+5sENKL07M4YR/+RBW9ysYMyI0kkSFfEfTinuXRIDkMHGQw59nHfRJKYwNsqR8fX3aJfqTC4y3iKqwmW/i8t4oU+/BfoYQpVwllxeVkRGh/HuWXCff5gS4n79IjF6qAyhjykZlqAHin1uspr+4CvzHmpQXWL766jIVe28qT5nIZqhEwEMkGttVMuBMjp6YUlULnZ5f5s9vIw==",
 * "secret_digit_signature": "ITglXEFsA7ML5+L8wqoE01MfF5EqaXQyBSkVYPBhXxtZQBQhSPJA43a5qwuiHc9F++1qlf8yWRxQ1fGSF0+W7pcw6OiIs7nDzE7RcIyZnekmzIiBp4vXEJ3CXq6T0d/QYHhP/FGn0Tn7kf+dY+cLaXYb+OmohI/qT5Pw18oOWuI=",
 * "weixin_auth": true,
 * "qq_auth": false,
 * "gionee_auth": false,
 * "estimated_media_num": 20033,
 * "membership": 0,
 * "gender": 1,
 * "estimate_gender": "male",
 * "vip_level": 99,
 * "vip_desc": "你的会员已过期",
 * "created_at": "2017-09-06T15:58:40+08:00",
 * "days_from_created": 1680,
 * "trash_show_days": 30,
 * "max_file_size": 314572800,
 * "cluster_threshold": 0.43747727084867516,
 * "member_ad_url": "https://lf6-everphoto.bytetos.com/obj/everphoto-statics/images/member_ad_banner_be_vip_v2.png"
 * }
 * }
 * }
 */
@Data
public class HttpResult<T> {
    public Integer timestamp;
    public Integer code;
    public String message;
    public T data;

    public boolean isSuccess() {
        return code == EverPhotoConstant.SUCCESS_CODE;
    }
}
