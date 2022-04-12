# -*- coding:utf-8 -*-
import base64
import json
import random
import re
import time

import requests
import rsa

s = requests.Session()

def fun1():
    rand_str = str(round(time.time() * 1000))
    url = f"https://api.cloud.189.cn/mkt/userSign.action?rand={rand_str}&clientType=TELEANDROID&version=8.7.2"

    headers = {
        'Host': 'm.cloud.189.cn',
        'User-Agent': 'Ecloud/8.7.2 Android/28',
        'Content-Type': 'text/xml; charset=utf-8'
    }

    response = s.get(url, headers=headers)
    res_data = json.loads(response.text)
    netdiskBonus = res_data["netdiskBonus"]
    if res_data["isSign"]:
        print(f"今日已签到,获得{netdiskBonus}M空间")
        return  f"签到获得{netdiskBonus}M,"
    else:
        print(f"签到获得{netdiskBonus}M空间")
        return f"签到获得{netdiskBonus}M,"



def fun2(taskId):
    random_str = str(random.random())
    url = f"https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId={taskId}&activityId=ACT_SIGNIN&noCache={random_str}"

    headers = {
        'Host': 'm.cloud.189.cn',
        'User-Agent': 'Mozilla/5.0 (Linux; Android 9) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 Ecloud/8.7.2 Android/28 proVersion/1.0.6',
        'X-Requested-With': 'XMLHttpRequest',
        'Referer': 'https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1'
    }

    response = s.get(url, headers=headers)
    res_data = json.loads(response.text)

    if "prizeName" in res_data:
        description = res_data["prizeName"]
        print(f"抽奖获得{description}")
        return f"抽奖{description},".replace("天翼云盘", "").replace("空间", "")
    else:
        if res_data["errorCode"] == "User_Not_Chance":
            print("抽奖次数已用完")
            return "次数用完,"
        else:
            print(response.text.encode('utf8'))


BI_RM = "0123456789abcdefghijklmnopqrstuvwxyz"


def int2char(a):
    return BI_RM[a]


b64map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
b64pad = "="


def b64tohex(a):
    c = 0
    d = ""
    e = 0
    a_len = len(a)
    for b in range(a_len):
        if a[b] != b64pad:
            v = b64map.index(a[b])
            if v < 0:
                pass
            else:
                if e == 0:
                    d += int2char(v >> 2)
                    c = 3 & v
                    e = 1
                elif e == 1:
                    d += int2char(c << 2 | v >> 4)
                    c = 15 & v
                    e = 2
                elif e == 2:
                    d += int2char(c)
                    d += int2char(v >> 2)
                    c = 3 & v
                    e = 3
                else:
                    d += int2char(c << 2 | v >> 4)
                    d += int2char(15 & v)
                    e = 0
    if e == 1:
        d += int2char(c << 2)
    return d


def rsa_encode(j_rsakey, string):
    rsa_key = f"-----BEGIN PUBLIC KEY-----\n{j_rsakey}\n-----END PUBLIC KEY-----"
    pubkey = rsa.PublicKey.load_pkcs1_openssl_pem(rsa_key.encode())
    result = b64tohex((base64.b64encode(rsa.encrypt(f'{string}'.encode(), pubkey))).decode())
    return result


def login(uname, upwd):
    j_rsaKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDY7mpaUysvgQkbp0iIn2ezoUyhi1zPFn0HCXloLFWT7uoNkqtrphpQ/63LEcPz1VYzmDuDIf3iGxQKzeoHTiVMSmW6FlhDeqVOG094hFJvZeK4OzA6HVwzwnEW5vIZ7d+u61RV1bsFxmB68+8JXs3ycGcE4anY+YzZJcyOcEGKVQIDAQAB"
    username = rsa_encode(j_rsaKey, uname)
    password = rsa_encode(j_rsaKey, upwd)

    url = "https://cloud.189.cn/api/portal/loginUrl.action?redirectURL=https://cloud.189.cn/web/redirect.html"
    r = s.get(url)
    html_str = r.text

    captchaToken = re.findall(r"captchaToken' value='(.+?)'", html_str)[0]
    lt = re.findall(r'lt = "(.+?)"', html_str)[0]
    returnUrl = re.findall(r"returnUrl = '(.+?)'", html_str)[0]
    paramId = re.findall(r'paramId = "(.+?)"', html_str)[0]
    s.headers.update({"lt": lt})

    url2 = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36',
        'Content-Type': 'application/x-www-form-urlencoded',
        'Referer': 'https://open.e.189.cn/'
    }
    payload = {
        "appKey": "cloud",
        "accountType": '01',
        "userName": f"{{RSA}}{username}",
        "password": f"{{RSA}}{password}",
        "validateCode": "",
        "captchaToken": captchaToken,
        "returnUrl": returnUrl,
        "mailSuffix": "@189.cn",
        "paramId": paramId
    }
    r = s.post(url2, headers=headers, data=payload, timeout=5)
    res_data = json.loads(r.text)
    print(res_data['msg'])

    redirect_url = res_data['toUrl']
    s.get(redirect_url)
    return s


def checkin(cloud_username, cloud_password):
    login(cloud_username, cloud_password)
    result=fun1()
    taskId = ["TASK_SIGNIN", "TASK_SIGNIN_PHOTOS"]
    for i in range(2):
        time.sleep(random.randint(800, 1200) / 1000)
        result += fun2(taskId[i])
    return result


def test():
    print("测试函数")
    # checkin("xxx", "xxx")


if __name__ == '__main__':
    test()
