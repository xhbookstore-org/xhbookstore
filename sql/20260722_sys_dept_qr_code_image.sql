-- 门店二维码图片地址；保存上传后的相对资源地址或完整 URL。
ALTER TABLE sys_dept
    ADD COLUMN qr_code_image_url VARCHAR(500) NULL COMMENT '门店二维码图片地址' AFTER erp_dept_id;
