## 1. redis 和lua的结合
 eval "script-content" key个数 key列表 args个数
 
 - 其中key是指redis中的key，
 - args是指随身携带的其他的参数，也可以是redis中的key