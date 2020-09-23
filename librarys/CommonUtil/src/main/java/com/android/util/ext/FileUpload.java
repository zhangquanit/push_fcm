package com.android.util.ext;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.util.log.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileUpload {

    public static final String CHARSET = "UTF-8";

    public static final int HTTP_OK = HttpURLConnection.HTTP_OK;
    /**
     * net work is unavailable
     */
    public static final int HTTP_REQUEST_NETWORK_ERROR = 1000;
    /***
     * error input when request excuted
     */
    public static final int HTTP_REQUEST_ERROR_INPUT = 1001;
    /***
     * error url address when request excuted
     */
    public static final int HTTP_REQUEST_ERROR_URL = 1003;
    /**
     * request cancel
     */
    public static final int HTTP_REQUEST_ERROR_CANCELED = 1004;
    /**
     * connect request failure
     */
    public static final int HTTP_REQUEST_ERROR_CONNECT = 1005;
    /****
     * request exception when excuted
     */
    public static final int HTTP_REQUEST_EXCEPTION = 1006;

    public static final int TIMEOUT = 15000;
    private static final int BUF_SIZE = 1024;

    private int connectOutTime = TIMEOUT;

    private HttpURLConnection mConnection = null;
    private boolean mStop = false;
    private int mRequestErrorCode = 0;
    private int mResponseCode = 0;
    private String mResultDesc = "unknown";

    protected Object objAbort = new Object();

    private String session;

    public FileUpload() {

    }

    public String postRequest(String strUrl, Map<String, String> params) {
        HttpURLConnection conn = null;
        DataOutputStream outStream = null;
        InputStream inStream = null;
        if (params == null) {
            return null;
        }
        try {
            String BOUNDARY = java.util.UUID.randomUUID().toString();
            String MULTIPART_FORM_DATA = "multipart/form-data";
            String PREFIX = "--", LINEND = "\r\n";

            URL url = new URL(strUrl);

            conn = getConnection(url);
            if (conn == null) {
                mRequestErrorCode = HTTP_REQUEST_ERROR_CONNECT;
                return null;
            }

            mConnection = conn;
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", CHARSET);
            // conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA +
            // "; boundary=" + BOUNDARY);

            try {
                conn.connect();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                checkStop();
                return null;
            }
            if (checkStop()) {
                return null;
            }

            // construct params for Text
            // StringBuilder sb = new StringBuilder();
            // for (Map.Entry<String, String> entry : params.entrySet()) {
            // sb.append(PREFIX);
            // sb.append(BOUNDARY);
            // sb.append(LINEND);
            // sb.append("Content-Disposition: form-data; name=\"" +
            // entry.getKey() + "\"" + LINEND);
            // sb.append("Content-Type: text/plain; charset=" + CHARSET +
            // LINEND);
            // sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            // sb.append(LINEND);
            // sb.append(entry.getValue());
            // sb.append(LINEND);
            // }
            StringBuffer sb = new StringBuffer();
            // 组织请求参数
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry element = (Map.Entry) it.next();
                sb.append(element.getKey());
                sb.append("=");
                sb.append(element.getValue());
                sb.append("&");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes(CHARSET));
            outStream.flush();

            if (checkStop()) {
                return null;
            }

            mResponseCode = conn.getResponseCode();
            mResultDesc = conn.getResponseMessage();
            if (mResponseCode != HttpURLConnection.HTTP_OK) {
                mResultDesc = conn.getResponseMessage();
                return null;
            }

            if (checkStop()) {
                return null;
            }

            inStream = conn.getInputStream();
            return getResponseData(inStream);
        } catch (SocketException e) {
            e.printStackTrace();
            mRequestErrorCode = HTTP_REQUEST_NETWORK_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            mRequestErrorCode = HTTP_REQUEST_NETWORK_ERROR;
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                    outStream = null;
                }
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkStop()) {
                synchronized (objAbort) {
                    objAbort.notify();
                }
            }
        }

        return null;
    }

    public String getRequest(String url) {
        // TODO Auto-generated method stub
        if (TextUtils.isEmpty(url)) {
            mRequestErrorCode = HTTP_REQUEST_ERROR_INPUT;
            return null;
        }

        URL getUrl = null;
        try {
            getUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mRequestErrorCode = HTTP_REQUEST_ERROR_URL;
            return null;
        }

        mStop = false;
        InputStream input = null;
        HttpURLConnection conn = null;
        try {
            conn = getConnection(getUrl);
            if (conn == null) {
                mRequestErrorCode = HTTP_REQUEST_ERROR_CONNECT;
                return null;
            }
            mConnection = conn;
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(getConnectTimeOut());
            conn.setReadTimeout(getConnectTimeOut());
            conn.setDoInput(true);

            if (checkStop()) {
                return null;
            }

            try {
                conn.connect();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                checkStop();
                return null;
            }
            if (checkStop()) {
                return null;
            }

            mResponseCode = conn.getResponseCode();
            mResultDesc = conn.getResponseMessage();
            return getResponseData(conn.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                    input = null;
                }
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkStop()) {
                synchronized (objAbort) {
                    objAbort.notify();
                }
            }
        }
        if (!checkStop()) {
            mRequestErrorCode = HTTP_REQUEST_EXCEPTION;
        }
        return null;
    }

    public String postRequest(String strUrl, byte[] data) {
        // TODO Auto-generated method stub
        HttpURLConnection conn = null;
        OutputStream outStream = null;
        InputStream inStream = null;
        try {
            URL url = new URL(strUrl);
            conn = getConnection(url);
            if (conn == null) {
                mRequestErrorCode = HTTP_REQUEST_ERROR_CONNECT;
                return null;
            }
            conn.setRequestProperty("Cookie", this.session);
            // user stop
            if (checkStop()) {
                return null;
            }
            mConnection = conn;
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(getConnectTimeOut());
            conn.setReadTimeout(getConnectTimeOut());
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", CHARSET);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try {
                conn.connect();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                checkStop();
                return null;
            }
            if (checkStop()) {
                return null;
            }

            if (data != null) {
                outStream = conn.getOutputStream();
                outStream.write(data);
                outStream.close();
            }
            // 获取cookie
            saveSession(conn);
            mResponseCode = conn.getResponseCode();
            mResultDesc = conn.getResponseMessage();
            if (mResponseCode != HttpURLConnection.HTTP_OK) {
                mResultDesc = conn.getResponseMessage();
                return null;
            }

            if (checkStop()) {
                return null;
            }

            inStream = conn.getInputStream();
            return getResponseData(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            mResultDesc = e.getMessage();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                    outStream = null;
                }
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkStop()) {
                synchronized (objAbort) {
                    objAbort.notify();
                }
            }
        }

        return null;
    }

    private void saveSession(HttpURLConnection conn) {
        // 获取cookie
        Map<String, List<String>> map = conn.getHeaderFields();
        String firstCookie = null;
        if (map != null) {
            Set<String> set = map.keySet();
            for (Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                if (key != null && key.equals("Set-Cookie")) {
                    LogUtil.d("key=" + key + ",开始获取cookie");
                    List<String> list = map.get(key);
                    StringBuilder builder = new StringBuilder();
                    for (String str : list) {
                        builder.append(str).toString();
                    }
                    firstCookie = builder.toString();
                    LogUtil.d("cookie------" + firstCookie);
                }
            }
        }
        this.session = firstCookie;
    }

    public String getSession() {
        // TODO Auto-generated method stub
        return this.session;
    }

    public void setSessionToHeader(String session) {
        // TODO Auto-generated method stub
        this.session = session;
    }

    public String postFileRequest(String strUrl, byte[] data, String filePath) {

        HttpURLConnection conn = null;
        OutputStream outStream = null;
        InputStream inStream = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            String BOUNDARY = java.util.UUID.randomUUID().toString();
            String MULTIPART_FORM_DATA = "multipart/form-data";
            String PREFIX = "--", LINEND = "\r\n";

            URL url = new URL(strUrl);

            conn = getConnection(url);
            if (conn == null) {
                mRequestErrorCode = HTTP_REQUEST_ERROR_CONNECT;
                return null;
            }
            // user stop
            if (checkStop()) {
                return null;
            }

            // Length
            int contentLength = (int) file.length();
            if (data != null) {
                contentLength += data.length;
                contentLength += 8;
            }

            mConnection = conn;
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(getConnectTimeOut());
            conn.setReadTimeout(getConnectTimeOut());
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
            // conn.setRequestProperty("Content-Length",""+contentLength);

            try {
                conn.connect();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                checkStop();
                return null;
            }
            if (checkStop()) {
                return null;
            }

            outStream = new BufferedOutputStream(conn.getOutputStream());

            if (data != null) {
                outStream.write(data);
                outStream.flush();
            }

            BufferedInputStream bis = null;
            try {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"" + filePath + "\"; filename=\"" + filePath + "\""
                        + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes(CHARSET));

                bis = new BufferedInputStream(new FileInputStream(file));
                byte[] temp = new byte[BUF_SIZE];
                int nReadLength = 0;
                int postLength = 0;
                while ((nReadLength = bis.read(temp)) != -1) {
                    // user stop
                    if (checkStop()) {
                        return null;
                    }

                    outStream.write(temp, 0, nReadLength);
                    outStream.flush();
                    postLength += nReadLength;
                }
                // outStream.write((newLine + divLine).getBytes());
            } catch (Exception e) {
                Log.e("TAG", e.getMessage());
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                        bis = null;
                    }
                } catch (Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            }

            outStream.write(LINEND.getBytes(CHARSET));
            // outStream.write(divLine.getBytes());
            outStream.flush();

            // the finsh flag
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes(CHARSET);
            outStream.write(end_data);
            outStream.flush();

            mResponseCode = conn.getResponseCode();
            mResultDesc = conn.getResponseMessage();
            if (mResponseCode != HttpURLConnection.HTTP_OK) {
                mResultDesc = conn.getResponseMessage();
                return null;
            }

            if (checkStop()) {
                return null;
            }

            inStream = conn.getInputStream();
            return getResponseData(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            mResultDesc = e.getMessage();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                    outStream = null;
                }
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkStop()) {
                synchronized (objAbort) {
                    objAbort.notify();
                }
            }
        }

        return null;
    }

    public String postParamAndFile(String actionUrl, Map<String, String> params, Map<String, File> files) {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        InputStream inStream = null;
        DataOutputStream outStream = null;
        HttpURLConnection conn = null;
        try {
            URL uri = new URL(actionUrl);
            conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(getConnectTimeOut()); // cache max time
            conn.setConnectTimeout(getConnectTimeOut());
            // conn.setDoInput(true);// allow input
            conn.setDoOutput(true);// allow output
            // conn.setChunkedStreamingMode(0);
            conn.setUseCaches(false); // cache is disable
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
            // conn.setRequestProperty("Accept-Encoding", "identity");
            conn.connect();
            outStream = new DataOutputStream(conn.getOutputStream());
            // send data
            if (files != null) {
                writeFileParams(outStream, BOUNDARY, files);
            }
            // construct params for Text
            if (params != null) {
                writeStringParams(outStream, BOUNDARY, params);
            }
            // the finsh flag
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            inStream = conn.getInputStream();
            return getResponseData(inStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                    outStream = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    // 普通字符串数据
    private void writeStringParams(DataOutputStream outputStream, String boundary, Map<String, String> textParams)
            throws Exception {
        Set<String> keySet = textParams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            String value = textParams.get(name);
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"\r\n");
            outputStream.writeBytes("\r\n");
            outputStream.writeBytes(URLEncoder.encode(value, "UTF-8") + "\r\n");
        }
    }

    /**
     * 文件数据
     *
     * @param outputStream
     * @param boundary
     * @param fileparams
     * @throws Exception
     */
    private void writeFileParams(DataOutputStream outputStream, String boundary, Map<String, File> fileparams)
            throws Exception {
        Set<String> keySet = fileparams.keySet();
        for (Iterator<String> it = keySet.iterator(); it.hasNext(); ) {
            String name = it.next();
            File value = fileparams.get(name);
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name
                    + "\"; filename=\"" + URLEncoder.encode(value.getName(), "UTF-8") + "\"\r\n");
            outputStream.writeBytes("Content-Type: " + "image/jpg" + "\r\n");
            outputStream.writeBytes("\r\n");
            outputStream.write(getBytes(value));
            outputStream.writeBytes("\r\n");
        }
    }

    // 把文件转换成字节数组
    private byte[] getBytes(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        in.close();
        return out.toByteArray();
    }

    public void cancel() {
        // TODO Auto-generated method stub
        mStop = true;
    }

    public int getResponseCode() {
        // TODO Auto-generated method stub
        return mResponseCode;
    }

    public String getResponseMessage() {
        // TODO Auto-generated method stub
        return mResultDesc;
    }

    /**
     * return request error code
     *
     * @return
     * @Param niufei
     * @Param 2014-5-11
     */

    public int getRequestErrorCode() {
        return mRequestErrorCode;
    }

    public boolean getResponseFile(final String strUrl, String filePath) {
        if (TextUtils.isEmpty(strUrl)) {
            mRequestErrorCode = HTTP_REQUEST_ERROR_INPUT;
            return false;
        }

        URL getUrl = null;
        try {
            getUrl = new URL(strUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mRequestErrorCode = HTTP_REQUEST_ERROR_URL;
            return false;
        }

        mStop = false;
        InputStream input = null;
        HttpURLConnection conn = null;
        try {
            conn = getConnection(getUrl);
            if (conn == null) {
                mRequestErrorCode = HTTP_REQUEST_ERROR_CONNECT;
                return false;
            }
            mConnection = conn;
            conn.setConnectTimeout(getConnectTimeOut());
            conn.setReadTimeout(getConnectTimeOut());
            conn.setDoInput(true);

            if (checkStop()) {
                return false;
            }

            try {
                conn.connect();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                checkStop();
                return false;
            }
            if (checkStop()) {
                return false;
            }

            mResponseCode = conn.getResponseCode();
            mResultDesc = conn.getResponseMessage();

            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);

            return getResponse(conn.getInputStream(), fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                    input = null;
                }
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkStop()) {
                synchronized (objAbort) {
                    objAbort.notify();
                }
            }
        }
        if (!checkStop()) {
            mRequestErrorCode = HTTP_REQUEST_EXCEPTION;
        }
        return false;
    }

    private synchronized boolean checkStop() {
        if (mStop) {
            mRequestErrorCode = HTTP_REQUEST_ERROR_CANCELED;
            return true;
        }
        return false;
    }

    private boolean getResponse(InputStream input, OutputStream output) {
        if (input == null || output == null) {
            return false;
        }
        byte[] data = new byte[1024];
        int i = 0;
        try {
            while (!checkStop() && (i = input.read(data)) != -1) {
                output.write(data, 0, i);
                output.flush();

                if (checkStop()) {
                    return false;
                }
            }

            output.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (checkStop()) {
            return false;
        }
        return true;
    }

    private HttpURLConnection getConnection(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
            mResultDesc = e.getMessage();
            conn = null;
        }
        return conn;
    }

    @Deprecated
    public String[] getAPN(Context context) {
        if (context == null) {
            return null;
        }

        String[] apnInfo = new String[2];
        Cursor mCursor = context.getContentResolver().query(
                Uri.parse("content://telephony/carriers/preferapn"),
                null, null, null, null);
        if (mCursor != null) {
            try {
                mCursor.moveToFirst();
                apnInfo = new String[2];
                apnInfo[0] = mCursor.getString(mCursor.getColumnIndex("proxy"));
                apnInfo[1] = mCursor.getString(mCursor.getColumnIndex("port"));
            } catch (Exception ex) {
                ex.printStackTrace();
                mResultDesc = ex.getMessage();
            } finally {
                mCursor.close();
            }
        }

        if (apnInfo == null || TextUtils.isEmpty(apnInfo[0]) || TextUtils.isEmpty(apnInfo[1])) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return null;
            }

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info == null || !info.isAvailable()) {
                NetworkInfo[] infoAll = connectivity.getAllNetworkInfo();
                if (infoAll != null) {
                    for (int i = 0; i < infoAll.length; i++) {
                        if (infoAll[i].getState() == NetworkInfo.State.CONNECTED) {
                            apnInfo[0] = android.net.Proxy.getDefaultHost();
                            apnInfo[1] = String.valueOf(android.net.Proxy.getDefaultPort());
                            return apnInfo;
                        }
                    }
                }
            } else {
                apnInfo[0] = android.net.Proxy.getHost(context);
                apnInfo[1] = String.valueOf(android.net.Proxy.getPort(context));
            }
        }
        return apnInfo;
    }

    private String getResponseData(InputStream input) {
        if (input == null) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] tmpBuf = new byte[BUF_SIZE];
            int i = 0;
            while (!mStop && (i = input.read(tmpBuf)) != -1) {
                output.write(tmpBuf, 0, i);
            }
            if (checkStop()) {
                return null;
            }

            return output.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            mResultDesc = e.getMessage();
        }

        return null;
    }

    private int getConnectTimeOut() {
        return connectOutTime;
    }

    /**
     * 设置请求超时时间
     *
     * @param millisecond
     */

    public void setConnectOutTime(int millisecond) {
        // TODO Auto-generated method stub
        this.connectOutTime = millisecond;
    }
}
