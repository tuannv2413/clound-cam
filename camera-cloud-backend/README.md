# Lịch sử cập nhật
- 30/12/2021: bp-dangt - sửa size field search_text cl_user, tb_user tăng lên 10485760
- 28/12/2021: binhdv - bảng cl_user_mobile_notify_token thêm id, cập nhật kiểu dữ liệu cho token
- 09/12/2021: bp-dangt - sửa size field avatar cl_user tăng lên 10485760
- 20/12/2021: huydv - cap nhat is_delete vao cl_box và cl_camera để xóa mềm
- 18/12/2021: huydv - thêm trường id vào bảng customer_camera_permission
- 17/12/2021: huydv - thêm trường is_default vào bảng cl_group_camera
- 16/12/2021: Huydv - Thêm trường active vào cl_group_service
- 15/12/2021: Huydv: Bỏ trường tenat_id ở bảng cl_sysamdmin
- 13/12/2021: bp-dangt - thêm trường office, search_text vào bảng cl_user
- 09/12/2021: bp-dangt - thêm trường email, avatar(base64) vào bảng cl_user
- 13/12/2021: Huydv - thêm bảng trung gian giữa ng dùng và optino độ phân giải (n-n giữa option gói và ng dùng)
- 09/12/2021: Huydv - Bỏ trường teant_id ở bảng cl_role
- 08/12/2021: Huydv - Thêm trường ghi chú vào bảng cl_role, và bỏ tenant_id, created_time, created_by, update_time, updated_by ở permmison, sửa lại quan hệ box và media box
- 08/12/2021: Huydv - Thêm trường delete vào bảng cl_user dùng xóa mềm và role_type vào bảng role dùng để phân biện quyền đc tạo cho đối tượng user nào
- 05/12/2021: Huydv - update thingsboard lên phiên bản 3.3.2- Mọi người chạy lại lệnh "mvn clean install -DskipTests" để cài lại
- 05/12/2021: Huydv - Update bảng manage_box them thông tin createdtime, createdby, updatedtime, updatedby
- 04/12/2021: Huydv - Add file thingsboard.yml vào gitignore, ae clone source không thấy thingsboard.yml thì copy 1 file thingsboard.yml.txt ra xóa đuôi txt đi;
- 04/12/2021: Huydv - sửa đổi liên kết bản role và permission từ 1-n thành n-n ("Ae drop bảng cũ và gen lại db");
- 04/12/2021: Vu Tung Lam - sửa lại tên ràng buộc ở bảng cl_role_and_permission

# Setup source code
Docs install thingsboard: https://thingsboard.io/docs/user-guide/install/windows/
1. Install OpenJDK 11: https://adoptopenjdk.net/index.html
2. Install maven: https://maven.apache.org/install.html
3. Install postgres: version 11+ https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
<br> Username password nên đặt là: postgres/postgres nếu đặt khác thì sửa trong file thingsboard.yml
4. Run command "mvn clean install -DskipTests" để buid source thingsboard
5. Create database postgres: camera_cloud
6. Tiến hành chạy application/src/main/java/org/thingsboard/server/ThingsboardInstallApplication.java để generate db source code
7. Tiến hành chạy application/src/main/java/org/thingsboard/server/ThingsboardServerApplication.java để run code
<br>
NOTE: 
-Recommend sử dụng intellij để code
# Tuân thủ mô hình code:
- Không inject trực tiếp repository vào tầng service hoặc controller (entity phải được convert sang dto)
- Cài đặt solar lint lên IDE sử dụng trong quá trình code
- Thực hiện code trong thư mục dft 