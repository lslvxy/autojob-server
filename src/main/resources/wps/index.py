#!/usr/bin/env python
# -*- coding: utf-8 -*-

'''
@time      :2020/05/09 13:44:54
@author    :4rat
'''

import urllib3
import requests
import time
import json
import os
urllib3.disable_warnings()


def wps_invite(sid):
    '''
    签到
    sid可以登录http://zt.wps.cn，查看cookie里的sid进行获取
    '''
    url = "https://zt.wps.cn/2018/clock_in/api/clock_in?member=wps"
    headers = {
        'Host': 'zt.wps.cn',
        'content-type': 'application/json',
        'sid': sid,
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36'
    }
    r = requests.get(url, headers=headers, verify=False)
    try:
        html = json.loads(r.text)
        if html['result'] == 'ok':

            msglog = '[-]   签到状态: ' + html['result']+" wps签到打卡成功！" + \
                time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())+" \n\n"
            return msglog
        else:
            msglog = '[*]    签到状态:' + html['result']+' : '+html['msg']+" " + \
                time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())+" \n\n"
            return msglog
    except Exception:
        msglog = '[*]    请正确填写SID! \n\n'
        return msglog


def wps_sign_in(userid):
    '''
    邀请
    '''
    msgtextlog = ''
    url = "https://zt.wps.cn/2018/clock_in/api/invite"
    sids = [
        "V02SC1mOHS0RiUBxeoA8NTliH2h2NGc00a803c35002693584d",
        "V02StVuaNcoKrZ3BuvJQ1FcFS_xnG2k00af250d4002664c02f",
        "V02SWIvKWYijG6Rggo4m0xvDKj1m7ew00a8e26d3002508b828",
        "V02Sr3nJ9IicoHWfeyQLiXgvrRpje6E00a240b890023270f97",
        "V02SBsNOf4sJZNFo4jOHdgHg7-2Tn1s00a338776000b669579",
        "V02ScVbtm2pQD49ArcgGLv360iqQFLs014c8062e000b6c37b6",
        "V02S2oI49T-Jp0_zJKZ5U38dIUSIl8Q00aa679530026780e96",
        "V02ShotJqqiWyubCX0VWTlcbgcHqtSQ00a45564e002678124c",
        "V02SFiqdXRGnH5oAV2FmDDulZyGDL3M00a61660c0026781be1",
        "V02S7tldy5ltYcikCzJ8PJQDSy_ElEs00a327c3c0026782526",
        "V02SPoOluAnWda0dTBYTXpdetS97tyI00a16135e002684bb5c",
        "V02Sb8gxW2inr6IDYrdHK_ywJnayd6s00ab7472b0026849b17",
        "V02SwV15KQ_8n6brU98_2kLnnFUDUOw00adf3fda0026934a7f",
        "V02SBpDdos7QiFOs_5TOLF0a80pWt-U00a94ce2c003a814a17",
    ]
    i = 1
    for sid in sids:
        if i <= 10:
            headers = {
                'Host': 'zt.wps.cn',
                'content-type': 'application/json',
                'sid': sid,
                'Accept-Encoding': 'gzip, deflate',
                'Connection': 'close',
                'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36'
            }
            r = requests.post(
                url, headers=headers, data='{"invite_userid":"'+userid+'"}', verify=False)
            time.sleep(0.1)
            if r.status_code == 200:
                msglog = "[-]    邀请{}位好友, 成功, 状态码: {}, sid: {}\n\n".format(
                    i, r.status_code, sid)
                i += 1
                msgtextlog += msglog
            else:
                msglog = "[*]    邀请好友失败, 状态码: {}, sid: {}\n\n".format(
                    r.status_code, sid)
                msgtextlog += msglog
        else:
            break
    return msgtextlog


def main():
    userid = os.environ['username']
    sid = os.environ['password']
    # 调用Server酱推送消息
    invite = wps_invite(sid)
    sign_in = wps_sign_in(userid)
    content = invite + sign_in
    api = "https://sc.ftqq.com/[your-key].send"
    title = "Dk-WPS打卡通知"
    data = {
        "text": title,
        "desp": content
    }
    # req = requests.post(url=api, data=data)
    print(data)
    return content.encode('utf8')

def main_handler(event, context):
    return main()

if __name__ == "__main__":
    main(sid='V02SIvygq3oDepOtUwWRYeWfhhd3b0M00a0591610033791430', userid='863573040')
