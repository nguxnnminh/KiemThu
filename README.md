# Smart Dental - Quan ly phong kham nha khoa

He thong quan ly phong kham nha khoa, xay dung bang Spring Boot + Spring MVC +
Thymeleaf + Spring Security + Spring Data JPA + MySQL 8 + Flyway.

## 1. Yeu cau moi truong

- Java 17+
- Maven 3.9+ (du an dung Maven offline tai `C:\maven\bin\mvn.cmd`)
- XAMPP (bat MySQL trong XAMPP Control Panel)

## 2. Khoi dong database

Bat MySQL trong XAMPP Control Panel, sau do chay:

```cmd
db-start.cmd
```

Lenh nay tao database trong XAMPP MySQL voi cac thong tin:

- Database: `Clinic`
- User: `root`
- Password: de trong
- Port: `3306`

Neu MySQL da chay trong XAMPP, ung dung cung co the tu tao database `Clinic`
nho tham so `createDatabaseIfNotExist=true` trong `application.yml`.

## 3. Chay ung dung

```cmd
run.cmd
```

Lenh nay se `mvn clean package -DskipTests` roi chay file jar tao ra.
Flyway se tu dong tao schema (V1-V6) va seed du lieu demo khi ung dung khoi dong lan dau.

Hoac chay truc tiep bang Maven (khong build jar):

```cmd
C:\maven\bin\mvn.cmd spring-boot:run
```

Ung dung chay tai: http://localhost:8080

> Luu y: neu Windows bao loi "khong xoa duoc target\smart-dental-*.jar" hoac build bi khoa file,
> hay tat ung dung dang chay (Task Manager > ket thuc tien trinh `java.exe`) roi build lai.

## 4. Tai khoan demo

| Username   | Password      | Vai tro       |
|------------|---------------|---------------|
| admin      | Admin123      | ADMIN         |
| manager    | Manager123    | MANAGER       |
| reception  | Reception123  | RECEPTIONIST  |
| doctor     | Doctor123     | DOCTOR        |
| patient    | Patient123    | PATIENT       |

## 5. Cau truc du an

```text
src/main/java/com/smartdental/
├─ config/       Cau hinh Security, JPA Auditing, MVC
├─ controller/   Controller MVC
├─ dto/          Form/DTO
├─ entity/       JPA entity
├─ enums/        Enum trang thai/vai tro
├─ exception/    BusinessException, GlobalExceptionHandler
├─ repository/   Spring Data JPA repository
├─ security/     UserDetails, CustomUserDetails
├─ service/      Service nghiep vu (CodeGenerator, AuditLog, Auth, Payroll, ...)
└─ util/         Tien ich chung
```

## 6. Cac module chuc nang

- **Quan ly he thong** (UC1.x): nguoi dung, vai tro, danh muc dich vu, ca lam viec,
  phong/ghe, ngay nghi, audit log.
- **Quan ly lich kham** (UC2.x): dang ky ca truc bac si, dat lich hen, check-in benh nhan,
  xep phong/ghe theo ca truc da xac nhan.
- **Tiep don va kham benh** (UC3.x): kham benh, chan doan, so rang, ke dich vu,
  lap hoa don, thu tien/hoan tien.
- **Tinh luong bac si** (UC4.x): he so ca, he so ca phuc tap (duyet/tu choi),
  lap phieu luong, bao cao luong thang/nam, xuat Excel.

## 7. Chay test

```cmd
C:\maven\bin\mvn.cmd -o test
```

Test su dung profile `test` voi co so du lieu H2 (in-memory, MODE=MySQL), khong anh huong
den database `Clinic` tren XAMPP.

## 8. Lo trinh trien khai

- Phan 0: Cai dat, kien truc nen, bao mat, layout, database nen (DA HOAN THANH)
- Phan 1: Quan ly he thong (UC1.1 - UC1.4) (DA HOAN THANH)
- Phan 2: Quan ly lich kham (UC2.1 - UC2.6) (DA HOAN THANH)
- Phan 3: Tiep don va kham benh (UC3.1 - UC3.6) (DA HOAN THANH)
- Phan 4: Tinh luong bac si (UC4.1 - UC4.7) (DA HOAN THANH)
- Phan 5: Kiem thu tong the, hardening, hoan thien (DA HOAN THANH)
