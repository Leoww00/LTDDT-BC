HE THONG DAT DO AN KY TUC XA - JAVA OOP + GIAO DIEN WEB
BAN DA SUA LOI CHAY TREN INTELLIJ/VS CODE

*** CACH CHAY DE NHAT TREN WINDOWS ***
1. Giai nen file ZIP.
2. Vao thu muc DatDoAnKTX_Web_Java_DA_SUA_LOI (co src, web, MO_WEBSITE.bat).
3. NHAP DUP vao MO_WEBSITE.bat.
4. Trinh duyet se tu mo tai http://localhost:9090 (hoac cong trong gan do).
5. KHONG dong cua so den trong luc su dung website.

*** CACH CHAY BANG INTELLIJ IDEA ***
1. File > Open, chon thu muc DatDoAnKTX_Web_Java_DA_SUA_LOI.
2. Mo src/main/Main.java, bam tam giac xanh Run.
3. Xem duong dan duoc in trong khung Run. Website thuong tu mo.
4. Khong can sua Working directory hay sua WebServer.java.

*** LUU Y ***
- Java 17+ la du; may ban da co Java 21.
- Loi 8080/9090 bi chiem: chuong trinh tu thu cong tiep theo.
- Khong mo web/index.html truc tiep: phai chay Java de dat hang duoc.
- Luu don hang: data/orders.txt. Vi sinh vien la du lieu mau.
- Thanh toan tien mat/chuyen khoan chi la mo phong.
- Anh mon an tren Internet co the khong tai duoc neu mat mang.

*** CAU TRUC ***
src/model      : User, Student, Food, Cart, CartItem, Order, OrderDetail
src/service    : FoodService, OrderService va cac phuong thuc thanh toan
src/repository : FileRepository (ghi file)
src/utils      : Validation, IdGenerator, ProjectPaths
src/main       : Main, AppContext, WebServer
web            : index.html, style.css, app.js

Project tuong thich JDK 17+ va khong can MySQL/Spring Boot.
