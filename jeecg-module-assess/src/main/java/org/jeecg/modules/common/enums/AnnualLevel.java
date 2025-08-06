package org.jeecg.modules.common.enums;

/**
 * @Title: AnnualLevel
 * @Author Cikian
 * @Package org.jeecg.modules.common
 * @Date 2025/3/12 14:47
 * @description: jeecg-boot-parent: 年度考核等次
 */
public enum AnnualLevel {
    // 枚举常量定义：名称 + 显示值
    EXCELLENT("优秀") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("1");  // 等级1为“优秀”，所有类型通用
        }
    },
    COMPETENT("称职") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("2") && ("bureau".equals(type) || "basic".equals(type));
        }
    },
    QUALIFIED("合格") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("2") && "institution".equals(type);
        }
    },
    GOOD("良好") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("2") && "group".equals(type);
        }
    },
    BASIC_COMPETENT("基本称职") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("3") && ("bureau".equals(type) || "basic".equals(type));  // basic类型等级3对应“基本称职”
        }
    },
    BASIC_QUALIFIED("基本合格") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("3") && "institution".equals(type);
        }
    },
    COMMON("一般") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("3") && "group".equals(type);
        }
    },
    INCOMPETENCE("不称职") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("4") && ("bureau".equals(type) || "basic".equals(type));  // basic类型等级3对应“基本称职”
        }
    },
    UNQUALIFIED("不合格") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("4") && "institution".equals(type);
        }
    },
    BAD("较差") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("4") && "group".equals(type);
        }
    },
    UNCERTAIN("不确定等次") {
        @Override
        public boolean matches(String type, String level) {
            return level.equals("5");  // 等级5为“不确定等次”，所有类型通用
        }
    };

    private final String displayName;  // 显示名称（如“优秀”）

    AnnualLevel(String displayName) {
        this.displayName = displayName;
    }

    // 抽象方法：每个枚举常量需实现匹配逻辑
    public abstract boolean matches(String type, String level);

    // 根据类型和等级获取枚举值
    public static AnnualLevel getByTypeAndLevel(String type, String level) {
        for (AnnualLevel eval : values()) {
            if (eval.matches(type, level)) {
                return eval;
            }
        }
        throw new IllegalArgumentException("无效的类型或等级: type=" + type + ", level=" + level);
    }

    // 获取显示名称
    public String getDisplayName() {
        return displayName;
    }
}
