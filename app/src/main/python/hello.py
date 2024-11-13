import numpy as np # 追加
import pandas as pd # 追加
import os
from ultralytics import YOLO
# import sys
# import platform

def model_rode():
    # デルのロード (例: YOLOv10-M)
    try:
        # デルのロード (例: YOLOv10-M)
        model = YOLO("..yolov10s.pt")
        return "ロード成功"
    except Exception :
        return "ロード失敗"

def file_check():
    dir = os.path.dirname(os.path.abspath(__file__))
    return os.listdir(dir)


def hellow_model():
    # current_dir = os.getcwd()
    current_dir = os.path.abspath("hello.py")
    return current_dir

def hello_world():
    # return sys.version
    return "Hello takotako"

def set_text(txt):
    return txt

# ------ 以下の記述を追加 ------
def test_numpy():
    return np.array([1,2,3,4,5])

# ------ 以下の記述を追加 ------
def test_pandas():
    return pd.Series([1,2,3,4,5])