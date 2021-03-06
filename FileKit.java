/*
 *	Copyright (c) 2012, Yulong Information Technologies
 *	All rights reserved.
 *  
 *  @Project: CarBaoshijie
 *  @author: Robot	
 */
package com.ly.neuter.core.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 文件操作工具类
 */
public class FileKit {
    private static final String TAG = "FileKit";

    /**
     * 保存数据
     *
     * @param context
     * @param data
     * @param filename
     * @return
     */
    public static boolean save(Context context, String data, String filename) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        return save(context, data.getBytes(), filename);
    }

    /**
     * 保存数据
     *
     * @param context
     * @param data
     * @param filename
     * @return
     */
    public static boolean save(Context context, byte[] data, String filename) {
        boolean result = false;
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(data);
            fos.flush();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 读取文件, 获取文本数据
     *
     * @param context
     * @param filename
     * @return
     */
//	public static String getString(Context context, String filename) {
//		String result = null;
//		FileInputStream fis = null;
//		try {
//			if (exists(context, filename)) {
//				fis = context.openFileInput(filename);
//				result = StringKit.stream2String(fis);
//			}
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage());
//		} finally {
//			try {
//				if (fis != null) {
//					fis.close();
//				}
//			} catch (Exception e) {
//				Log.e(TAG, e.getMessage());
//			}
//		}
//		return result;
//	}

    /**
     * 保存数据对象
     *
     * @param context
     * @param object
     * @param filename
     * @return
     */
    public static boolean save(Context context, Object object, String filename) {
        boolean result = false;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            result = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + "");
            }
        }
        return result;
    }

    /**
     * 读取数据对象
     *
     * @param context
     * @param filename
     * @return
     */
    public static Object getObject(Context context, String filename) {
        Object object = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            if (exists(context, filename)) {
                fis = context.openFileInput(filename);
                ois = new ObjectInputStream(fis);
                object = ois.readObject();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + "");
            }
        }
        return object;
    }

    /**
     * 删除数据对象
     *
     * @param context
     * @param filename
     */
    public static boolean remove(Context context, String filename) {
        boolean flag = false;
        if (context == null || filename == null) {
            return flag;
        }
        File file = context.getFileStreamPath(filename);
        if (file != null && file.exists() && file.isFile()) {
            flag = file.delete();
        }
        return flag;
    }

    /**
     * 判断文件是否存在（应用内部目录）
     *
     * @param context
     * @param filename
     * @return
     */
    public static boolean exists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file != null && file.exists();
        // String path = context.getFilesDir().getPath() + "/" + filename;
        // File file = new File(path);
        // return file.exists();
    }
    
        private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private Bitmap decodeBitmap(byte[] data, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
}
