# -*- coding:utf-8 -*-
import json
import os

import MyTianYi


def main():
    username = os.environ['username']
    password = os.environ['password']
    if username is None or password is None:
        return "请配置环境变量用户名和密码"
    else:
        return MyTianYi.checkin(username, password)



def main_handler(event, context):
    return main()


if __name__ == '__main__':
    main()