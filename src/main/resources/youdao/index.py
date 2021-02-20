import requests
import json
import time
import hashlib
import os
from requests.packages.urllib3.exceptions import InsecureRequestWarning
# 禁用安全请求警告
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

# 配置各种key
# Server酱申请的skey
SCKEY =  os.environ['SCKEY']
# 钉钉机器人的 webhook
webhook = os.environ['webhook']

# 配置通知方式 0=dingding 1=weixin 2=全都要 其他为不推送
notice = ''
#os.environ['notice']
#账号
username = os.environ['username']
#密码
password = os.environ['password']

global contents
contents = ''


def output(content):
    global contents
    content += '  '
    contents += content + '\n'
    content += '  '
    print(content)


#server酱推送
def server():
    global contents
    message = {"text": "有道云笔记签到通知！", "desp": contents}
    r = requests.post("https://sc.ftqq.com/" + SCKEY + ".send", data=message)
    if r.status_code == 200:
        print('[+]server酱已推送，请查收')


#钉钉消息推送
def dingtalk():
    webhook_url = webhook
    dd_header = {"Content-Type": "application/json", "Charset": "UTF-8"}
    global contents
    dd_message = {
        "msgtype": "text",
        "text": {
            "content": f'有道云笔记签到通知！\n{contents}'
        }
    }
    r = requests.post(url=webhook_url,
                      headers=dd_header,
                      data=json.dumps(dd_message))
    if r.status_code == 200:
        print('[+]钉钉消息已推送，请查收  ')


def sign():
    login_url = 'https://note.youdao.com/login/acc/urs/verify/check?app=web&product=YNOTE&tp=urstoken&cf=6&fr=1&systemName=&deviceType=&ru=https%3A%2F%2Fnote.youdao.com%2FsignIn%2F%2FloginCallback.html&er=https%3A%2F%2Fnote.youdao.com%2FsignIn%2F%2FloginCallback.html&vcode=&systemName=mac&deviceType=MacPC&timestamp=1611466345699'
    parame = {
        'username': username,
        'password': hashlib.md5(password.encode('utf8')).hexdigest(),
    }
    session = requests.session()
    # 登录
    a = session.post(url=login_url, data=parame, verify=False)
    # 签到
    checkin_url = 'http://note.youdao.com/yws/mapi/user?method=checkin'
    r = session.post(url=checkin_url)
    if r.status_code == 200:
        # 格式话
        info = json.loads(r.text)
        # 一共签到获得
        total = info['total'] / 1048576
        # 本次签到获得空间
        space = info['space'] / 1048576
        # 当前时间
        times = time.strftime('%Y-%m-%d %H:%M:%S',
                              time.localtime(info['time'] / 1000))
        output('[+]用户： ' + username + ' 签到成功!')
        output('[+]当前签到时间：' + times)
        output('[+]签到获得：' + str(space) + 'MB')
        output('[+]总共获得：' + str(total) + 'MB')


def main():
    output('---开始【有道云笔记每日签到】---')
    sign()
    output('---结束【有道云笔记每日签到】---')
    if notice == '0':
        try:
            dingtalk()
        except Exception:
            print('[+]请检查钉钉配置是否正确')
    elif notice == '1':
        try:
            server()
        except Exception:
            print('[+]请检查server酱配置是否正确')
    elif notice == '2':
        try:
            dingtalk()
        except Exception:
            print('[+]请检查钉钉配置是否正确')
        try:
            server()
        except Exception:
            print('[+]请检查server酱配置是否正确')
    else:
        print('[+]选择不推送信息')


def main_handler(event, context):
    return main()


if __name__ == '__main__':
    main()
