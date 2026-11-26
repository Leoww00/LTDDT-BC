KTX FOOD - PHIEN BAN MO RONG (JAVA OOP + WEB)
=============================================

1. Giai nen file ZIP.
2. Mo thu muc KTX_FOOD_Day_Du_Lich_Su_Ship_Voucher.
3. NHAP DUP VAO FILE MO_WEBSITE.bat.
4. Giu nguyen cua so lenh dang mo.
5. Website tu mo; neu khong, mo Chrome tai http://localhost:9090
   (Neu cong 9090 dang ban, xem dia chi port moi hien trong cua so lenh.)

TAI KHOAN THU:
 SV001 / 123456
 SV002 / 123456

DU LIEU DEMO, KHONG NAP/CHUYEN TIEN THAT.

GIO DAT HANG: 05:00 - truoc 23:00, gio Viet Nam.

TINH NANG MOI:
 - Lich su mua hang: luu ca mon, nguoi nhan, dia diem, phi ship, voucher.
 - Lich su thanh toan: phan biet thanh toan vi va cac phuong thuc cho xac nhan.
 - Lich su nap vi: luu yeu cau nap, ngay gio, trang thai.
 - Dat ho: nhap ten va so dien thoai nguoi nhan.
 - Ship: giao trong khuon vien KTX, phi demo 8.000d / don.
 - Voucher: KTX10, FREESHIP (quy tac ghi trong giao dien).
 - Gio gioi han tao don 05:00-23:00 duoc kiem tra trong Java.

CHUY Y: Ship, voucher va xac nhan ngan hang chi la mo phong de tai.
Khong co shipper that hoac API doi soat ngan hang that.


HUONG DAN TINH NANG MOI
=====================

1) LICH SU MUA
  Dang nhap > bam "Lich su mua" o menu hoac o cuoi gio hang.
  Du lieu doc lai tu data/orders.txt sau moi lan khoi dong.
  Moi don luu: ma don, thoi gian, nguoi nhan, mon, phi ship, voucher, tong tien.

2) LICH SU THANH TOAN
  Bam "Lich su thanh toan". Nguon du lieu la don hang da luu.
  Vi: trang thai "Da thanh toan" (mo phong).
  Tien mat: "Cho thanh toan tien mat".
  Chuyen khoan: "Cho xac nhan chuyen khoan" (co QR demo).
  Khong tu cho rang da chuyen tien chi vi da tao QR.

3) LICH SU NAP VI
  Bam "Lich su nap" hoac vao "Nap vi". Du lieu luu tai data/student_wallets.tsv.
  Nap tu ngan hang la NGAN HANG GIA LAP; bam xac nhan moi cong tien.

4) DAT HO
  Tick "Dat ho ban be / nguoi than", dien ten va so dien thoai (10 chu so).
  Tai khoan dang dang nhap van la nguoi tra tien neu chon thanh toan vi.

5) NHAN HANG / SHIP
  Tu nhan tai can tin: phi = 0.
  Ship toi phong KTX: phi = 8.000 VND (chi la muc GIA LAP).
  Khi dat ho va ship, nhap phong / toa KTX cua nguoi nhan.

6) VOUCHER DEMO
  KTX10: don mon >= 50.000 VND, giam 10% tien mon toi da 15.000 VND.
  FREESHIP: don mon >= 60.000 VND, chi dung khi chon ship; giam toan bo phi ship.
  Moi don dung toi da 1 ma, khong cong don voucher.

7) GIO DAT HANG
  Java cho phep tao don tu 05:00:00 den 22:59:59 gio VN.
  Tu 23:00:00 den truoc 05:00:00 khong cho tao don.
  Ngoai gio van co the xem menu, xem lich su, nap vi va sua gio hang.
  Nguon thoi gian duoc lay tai server (khong tin gio may client).

PACKAGE:
  model - User, Student, Food, Cart, Order, OrderOptions, OrderQuote...
  service - OrderService, AccountService, PaymentMethod...
  repository - FileRepository, AccountRepository
  utils - IdGenerator, ProjectPaths, Validation...
  main - Main, AppContext, WebServer

MINH HOA KIEM THU:
  javac -encoding UTF-8 -cp out -d out tests/TestGioDatHang.java
  java -cp out TestGioDatHang
  (Windows: sau khi mo website va script da bien dich xong)

KHONG SU DUNG DE GIAO DICH TIEN THAT / KINH DOANH THUC TE.
