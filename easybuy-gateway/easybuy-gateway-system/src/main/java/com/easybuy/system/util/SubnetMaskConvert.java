package com.easybuy.system.util;

import java.util.ArrayList;

public class SubnetMaskConvert {
    /**
     * 将标识位转换成子网掩码的整型数组
     *  方法：将子网掩码，如255.255.255.0，分成4组，根据标识位分别生成
     * @param s
     * @return
     */
    public static Integer[] toSubnetMask(String s) {
        //将String的标识位转换成整形
        var area = Integer.parseInt(s);
        //计算有几组二进制11111111(int:255)
        var group = area / 8;
        //计算剩下的组内有几位二进制1
        var power = area % 8;
        //声明一个有序容器，方便最后转换成数组
        var list = new ArrayList<Integer>();
        //大于等于1组
        if (group > 0) {
            for (var i = 0; i < group; i++) {
                //向容器内添加对应的255
                list.add(255);
            }
        }
        //剩下组内位数大于1，计算对应的int
        if (power > 0) {
            var total = 0;
            var each = 128;
            //从组内最高位开始计算，2的7次幂
            for (var i = 0; i < power; i++) {
                total += each;
                each /= 2;
            }
            //添加到容器的末尾
            list.add(total);
        }
        //如果容器内不足4组，则在末尾添加0
        if (list.size() < 4) {
            for (var i = list.size(); i < 4; i++) {
                list.add(0);
            }
        }
        //将容器转成数组返回
        return list.toArray(new Integer[0]);
    }
}
