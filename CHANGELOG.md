# Nhật ký thay đổi

## 0.1.0 - Phiên bản nền móng

- Khởi tạo repository độc lập, không dùng lõi TalkBack.
- Thêm `AccessibilityService` tương thích Android 10.
- Thêm TTS hệ thống và đọc sự kiện cửa sổ/mục giao diện.
- Thêm màn hình cài đặt riêng, không trỏ ngược vào trang Trợ năng.
- Thêm các nút hành động quay lại, trang chủ và ứng dụng gần đây.
- Thêm GitHub Actions build APK và phát hành thủ công lên GitHub Releases.
- Đặt minSdk 26 và targetSdk 30.

## 0.2.0 - Cử chỉ và phản hồi âm thanh

- Thêm điều hướng mục trước/mục sau bằng vuốt trái/phải.
- Thêm cuộn lên/xuống bằng cử chỉ và hành động scroll của node.
- Thêm chạm khám phá qua `TYPE_VIEW_HOVER_ENTER`, đọc nhãn và đặt accessibility focus.
- Thêm nhấn hai lần trên mục đang khám phá để gọi `ACTION_CLICK`.
- Thêm earcon cho focus, vuốt, kích hoạt và lỗi bằng `ToneGenerator` của Android.
- Bổ sung đọc vai trò cơ bản, trạng thái đã chọn và trạng thái bị vô hiệu hóa.
- Giữ màn hình cài đặt riêng, TTS hệ thống và target Android 10.
- Bổ sung bản sửa điều hướng: sắp xếp các mục đọc được theo vị trí trên màn hình, dùng làm dự phòng khi `focusSearch()` không trả về mục kế tiếp và tự cuộn khi chạm biên vùng danh sách.

## Lộ trình tiếp theo

- Điều hướng mục trước/mục sau bằng vuốt trái/phải.
- Chạm khám phá và nhấn hai lần để kích hoạt mục đang focus.
- Earcon riêng cho focus, cử chỉ và kích hoạt.
- Bộ đọc cây giao diện ổn định hơn trên Android 10.
