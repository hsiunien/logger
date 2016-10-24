package wang.xiunian.android;


import android.util.Log;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class L {

    private static String TAG_PREFIX = "wallet_";
    private static LogLevel sLogLevel = LogLevel.WARN;
    private final static List<ILog> sLoggerList = new CopyOnWriteArrayList<ILog>() {
        {
            add(new DefaultLogger());
        }
    };

    public static boolean shouldLog() {
        return sLogLevel.level <= LogLevel.DEBUG.level;
    }

    public static void setLogLevel(LogLevel level) {
        sLogLevel = level;
    }

    public static void addLogger(ILog logger) {
        for (ILog l : sLoggerList) {
            if (logger.getClass() == l.getClass()) {
                return;
            }
        }
        sLoggerList.add(logger);
    }

    public static void v(Object... obj) {
        printSub(LogLevel.VERBOSE, "_", obj);
    }

    public static void v(String tag, Object... message) {
        printSub(LogLevel.VERBOSE, tag, message);
    }

    public static void d(Object... message) {
        printSub(LogLevel.DEBUG, "_", message);
    }

    public static void d(String tag, Object... message) {
        printSub(LogLevel.DEBUG, tag, message);
    }

    public static void i(Object... message) {
        printSub(LogLevel.INFO, "_", message);
    }

    public static void i(String tag, Object... message) {
        printSub(LogLevel.INFO, tag, message);
    }

    public static void w(Object... message) {

        printSub(LogLevel.WARN, "_", message);
    }

    public static void w(String tag, Object... message) {
        printSub(LogLevel.WARN, tag, message);
    }

    public static void e(Object... message) {
        printSub(LogLevel.ERROR, "_", message);
    }

    public static void e(String tag, Object... message) {
        printSub(LogLevel.ERROR, tag, message);
    }

    /**
     * 打印当前线程调用栈
     */
    public static void printStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n                   ");
        }
        d("stack", sb.toString());
    }

    private static void printSub(LogLevel type, String tag, Object... sub) {
        if (sub.length == 0) {
            sub = new String[]{tag};
            tag = "_";
        }
        String fixTag = "_".equals(tag) || sub.length == 0 ? TAG_PREFIX : TAG_PREFIX + tag;

        switch (type) {
            case VERBOSE:
                for (ILog logger : sLoggerList) {
                    logger.v(fixTag, sub);
                }
                break;
            case DEBUG:
                for (ILog logger : sLoggerList) {
                    logger.d(fixTag, sub);
                }
                break;
            case INFO:
                for (ILog logger : sLoggerList) {
                    logger.i(fixTag, sub);
                }
                break;
            case WARN:
                for (ILog logger : sLoggerList) {
                    logger.w(fixTag, sub);
                }
                break;
            case ERROR:
                for (ILog logger : sLoggerList) {
                    logger.e(fixTag, sub);
                }
                break;
//            case ASSERT:
//                Log.wtf(tag, sub);
//                break;
        }
    }


    public interface ILog {

        /**
         * 对于一些繁琐的日志显示
         *
         * @param tag
         * @param message
         */
        void v(String tag, Object... message);

        /**
         * 用于调试的日志,此类日志不会记录到文件
         *
         * @param tag
         * @param message
         */
        void d(String tag, Object... message);

        void i(String tag, Object... message);

        void w(String tag, Object... message);

        void e(String tag, Object... message);
    }

    private static class DefaultLogger implements ILog {

        @Override
        public void v(String tag, Object... message) {
            if (sLogLevel.level <= LogLevel.VERBOSE.level) {
                printShortString(LogLevel.VERBOSE, tag, message);
            }
        }

        @Override
        public void d(String tag, Object... message) {
            if (sLogLevel.level <= LogLevel.DEBUG.level) {
                printShortString(LogLevel.DEBUG, tag, message);
            }
        }

        @Override
        public void i(String tag, Object... message) {
            if (sLogLevel.level <= LogLevel.INFO.level) {
                printShortString(LogLevel.INFO, tag, message);
            }
        }

        @Override
        public void w(String tag, Object... message) {
            if (sLogLevel.level <= LogLevel.WARN.level) {
                printShortString(LogLevel.WARN, tag, message);
            }
        }

        @Override
        public void e(String tag, Object... message) {
            if (sLogLevel.level <= LogLevel.ERROR.level) {
                printShortString(LogLevel.ERROR, tag, message);
            }
        }

        private void printSub(LogLevel type, String tag, String sub) {
            switch (type) {
                case VERBOSE:
                    Log.v(tag, sub);
                    break;
                case DEBUG:
                    Log.d(tag, sub);
                    break;
                case INFO:
                    Log.v(tag, sub);
                    break;
                case WARN:
                    Log.w(tag, sub);
                    break;
                case ERROR:
                    Log.e(tag, sub);
                    break;
//            case ASSERT:
//                Log.wtf(tag, sub);
//                break;
            }
        }


        private void printShortString(LogLevel type, String tag, Object... obj) {
            String msg = "";
            String[] st = wrapperContent(tag, obj);
            msg = st[2] + st[1];
            int index = 0;
            int maxLength = 4000;
            int countOfSub = msg.length() / maxLength;

            if (countOfSub > 0) {  // The log is so long
                for (int i = 0; i < countOfSub; i++) {
                    String sub = msg.substring(index, index + maxLength);
                    printSub(type, tag, sub);
                    index += maxLength;
                }
                printSub(type, tag, msg.substring(index, msg.length()));
            } else {
                printSub(type, tag, msg);
            }
        }

        private String[] wrapperContent(String tag, Object... objects) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            int position = 7;
            StackTraceElement targetElement = stackTrace[position];
            String className = targetElement.getClassName();
            String[] classNameInfo = className.split("\\.");
            if (classNameInfo.length > 0) {
                className = classNameInfo[classNameInfo.length - 1] + ".java";
            }
            String methodName = targetElement.getMethodName();
            int lineNumber = targetElement.getLineNumber();
            if (lineNumber < 0) {
                lineNumber = 0;
            }
            String methodNameShort = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
            String msg = (objects == null) ? "Log with null object" : getObjectsString(objects);
            String headString = "[(" + className + ":" + lineNumber + ")#" + methodNameShort + " ] ";
            return new String[]{tag, msg, headString};
        }

        private String getObjectsString(Object... objects) {
            if (objects.length > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n");
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    if (object == null) {
                        stringBuilder.append("param").append("[").append(i).append("]").append(" = ").append("null").append("\n");
                    } else {
                        stringBuilder.append("param").append("[").append(i).append("]").append(" = ").append(parseObject(object)).append("\n");
                    }
                }
                return stringBuilder.toString();
            } else {
                Object object = objects[0];
                return object == null ? "null" : object.toString();
            }
        }

        private String parseObject(Object obj) {
            if (obj instanceof String) {
                return obj.toString();
            } else {
                Gson gson = new Gson();
                try {
                    return "type:" + obj.getClass().getSimpleName() + "\n" + gson.toJson(obj);
                } catch (Exception e) {
                    return "connot convert to json:" + e.getMessage();
                }
            }
        }
    }


    public enum LogLevel {
        VERBOSE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5);
        private int level;

        LogLevel(int level) {
            this.level = level;
        }
    }
}
