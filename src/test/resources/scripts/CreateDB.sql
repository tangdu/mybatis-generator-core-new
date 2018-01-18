--
--    Copyright 2006-2017 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

drop table t_user if exists;

CREATE TABLE  t_user(
  id BIGINT NOT NULL ,
  create_time DATETIME,
  update_time DATETIME NOT NULL ,
  create_person VARCHAR(32) NULL ,
  update_person VARCHAR(32) NULL,
  is_delete TINYINT NOT NULL ,
  mobile_phone VARCHAR(16) NULL,
  email VARCHAR(32) NULL,
  passwd VARCHAR(50) NULL,
  status TINYINT NULL,
  openid_weixin VARCHAR(45) NULL,
  last_login DATETIME NOT NULL,
  certification TINYINT NULL,
  PRIMARY KEY (id)
);
comment on table t_user is '用户';
comment on column t_user.id is 'ID';