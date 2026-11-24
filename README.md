# Trình đọc màn hình Android 10 độc lập

Đây là phiên bản thử nghiệm đầu tiên của một trình đọc màn hình viết mới bằng Java, không dùng lõi TalkBack. Mục tiêu là kiểm soát rõ ràng luồng khởi động, màn hình cài đặt và tương thích Android 10.

## Phiên bản 0.1.0

Bản đầu tiên cung cấp `AccessibilityService`, TTS của hệ thống, đọc các mục giao diện khi nhận sự kiện accessibility, hành động quay lại/trang chủ/ứng dụng gần đây, và màn hình cài đặt riêng. Đây là nền móng để bổ sung dần điều hướng trái/phải, chạm khám phá, nhấn hai lần và earcon.

Không nên coi bản 0.1.0 là bản thay thế hoàn chỉnh TalkBack. Trình đọc màn hình hoàn chỉnh cần xử lý rất nhiều loại cây giao diện và thiết bị.

## Cài đặt và bật

1. Cài APK trên Android 10.
2. Mở biểu tượng **Trình đọc màn hình Android 10**.
3. Nhấn **Mở cài đặt Trình đọc màn hình** để xem cài đặt riêng.
4. Vào **Cài đặt Android → Trợ năng → Ứng dụng đã cài đặt** và bật dịch vụ.
5. Quay lại ứng dụng để kiểm tra TTS và trạng thái dịch vụ.

## Build

GitHub Actions build `:app:assembleDebug`. Không commit thư mục `build/`, cache hoặc APK vào source.
