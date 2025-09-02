-- RBAC & 组织与资源 & 审计 基础表

CREATE TABLE IF NOT EXISTS sys_user (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(64) NOT NULL UNIQUE,
  password      VARCHAR(255) NOT NULL,
  nickname      VARCHAR(64),
  email         VARCHAR(128),
  phone         VARCHAR(32),
  status        TINYINT DEFAULT 1,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(64) NOT NULL UNIQUE,
  name        VARCHAR(64) NOT NULL,
  description VARCHAR(255),
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_permission (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(128) NOT NULL UNIQUE,
  name        VARCHAR(128) NOT NULL,
  type        VARCHAR(32)  NOT NULL COMMENT 'api|menu|btn',
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dept (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id  BIGINT DEFAULT NULL,
  name       VARCHAR(128) NOT NULL,
  sort_no    INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_dept_parent FOREIGN KEY (parent_id) REFERENCES sys_dept(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_dept (
  user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, dept_id),
  CONSTRAINT fk_user_dept_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_dept_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_app (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  code       VARCHAR(64) NOT NULL UNIQUE,
  name       VARCHAR(128) NOT NULL,
  enabled    TINYINT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_id     BIGINT NOT NULL,
  parent_id  BIGINT DEFAULT NULL,
  name       VARCHAR(128) NOT NULL,
  path       VARCHAR(255),
  permission VARCHAR(128),
  type       VARCHAR(16) NOT NULL COMMENT 'catalog|menu|button',
  icon       VARCHAR(64),
  sort_no    INT DEFAULT 0,
  visible    TINYINT DEFAULT 1,
  CONSTRAINT fk_menu_app FOREIGN KEY (app_id) REFERENCES sys_app(id),
  CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES sys_menu(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_audit_log (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  username   VARCHAR(64),
  action     VARCHAR(128),
  resource   VARCHAR(255),
  method     VARCHAR(16),
  ip         VARCHAR(64),
  result     VARCHAR(32),
  message    VARCHAR(512),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


