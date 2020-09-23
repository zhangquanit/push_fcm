
package com.android.util.ext;

import android.os.Build;
import android.text.TextUtils;

import com.android.util.log.LogUtil;

/**
 * 手机机型
 *
 * @author 张全
 */
public class PhoneModelUtil {

    /**
     * 获取手机当前型号
     *
     * @return
     */
    public static PhoneModel getModel() {
        PhoneModel model = null;
        PhoneModel[] models = PhoneModel.values();
        for (PhoneModel m : models) {
            boolean isSelf = checkMode(m.modelName);
            if (isSelf) {
                model = m;
                break;
            }
        }
        return model;
    }

    /**
     * <p>
     * <ul>
     * </br>1.VIVO
     * <li>
     * [brand=vivo,model=vivoY18L,manu=BBK,fingerprint=vivo/msm8226/msm8226:4.3
     * /JLS36C/eng.compiler.20140606.231134:user/dev-keys]</li> </br>2.Meizu
     * <li>
     * [brand=Meizu,model=M040,manu=Meizu,fingerprint=Meizu/meizu_mx2/mx2:4.4
     * .4/KTU84P/m04x.Flyme_OS_3.8.5.20141020181533:user/release-keys]</li>
     * </br>3.Lenovo
     * <li>[brand=Lenovo,model=Lenovo
     * A850,manu=LENOVO,fingerprint=Lenovo/aux/A850
     * :4.2.2/JDQ39/A850_S128_130929.1380446496:user/release-keys]</li>
     * <li></li>
     * <li></li>
     * </ul>
     * </p>
     *
     * @param phoneModel
     * @return
     */
    private static boolean checkMode(String phoneModel) {
        String brand = Build.BRAND;
        if (!TextUtils.isEmpty(brand) && phoneModel.equalsIgnoreCase(brand)) {
            return true;
        }
        String model = Build.MODEL;
        if (!TextUtils.isEmpty(model)) {
            if (phoneModel.equals(PhoneModel.XIAOMI.modelName)) {
                if (model.startsWith("MI")) {
                    return true;
                }
            } else {
                if (model.startsWith(phoneModel)) {
                    return true;
                }
            }
        }

        String manu = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manu) && phoneModel.equalsIgnoreCase(manu)) {
            return true;
        }
        String fingerprint = Build.FINGERPRINT;
        if (!TextUtils.isEmpty(fingerprint) && fingerprint.startsWith(phoneModel)) {
            return true;
        }
        return false;
    }

    public enum PhoneModel {
        XIAOMI("Xiaomi", "http://static.easou.com/apps/lock-screen/help/mi.html"),
        OPPO("oppo", "http://static.easou.com/apps/lock-screen/help/oppo.html"),
        VIVO("vivo", "http://static.easou.com/apps/lock-screen/help/vivo.html"),
        HUAWEI("huawei", "http://static.easou.com/apps/lock-screen/help/huawei.html"),
        MEIZU("Meizu", "http://static.easou.com/apps/lock-screen/help/meizu.html"),
        LENOVO("Lenovo", "http://static.easou.com/apps/lock-screen/help/lenovo.html");

        private PhoneModel(String modelName, String url) {
            this.modelName = modelName;
            this.helpUrl = url;
        }

        public String modelName;
        public String helpUrl;
    }

    public static boolean isXiaoMi() {
        return getModel() == PhoneModel.XIAOMI;
    }

    public static boolean isViVo() {
        return getModel() == PhoneModel.VIVO;
    }

    public static boolean isLenovo() {
        return getModel() == PhoneModel.LENOVO;
    }

    public static boolean isMeizu() {
        return getModel() == PhoneModel.MEIZU;
    }

    public static boolean isOppo() {
        return getModel() == PhoneModel.OPPO;
    }

    public static void printMode() {
        String brand = Build.BRAND;// 系统定制商
        String model = Build.MODEL;// 系统版本
        String manu = Build.MANUFACTURER;
        String fingerprint = Build.FINGERPRINT;
        StringBuffer sb = new StringBuffer();
        sb.append("[brand=" + brand + ",");
        sb.append("model=" + model + ",");
        sb.append("manu=" + manu + ",");
        sb.append("fingerprint=" + fingerprint + "]");
        LogUtil.d("model", sb.toString());

    }
}
