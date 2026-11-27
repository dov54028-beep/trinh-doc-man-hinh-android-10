# Nhật ký thay đổi

## 0.4.0 - Kết nối ReaderHub trong ứng dụng

- Thêm màn hình tài khoản ReaderHub ngay trong APK, gồm đăng ký, đăng nhập, đăng xuất và khôi phục phiên bằng token phiên đã lưu cục bộ.
- Hiển thị vai trò tài khoản; quản trị viên có thể xem danh sách tài khoản, còn nhà phát triển gốc có thể thăng chức hoặc hạ chức quản trị viên.
- Thêm API HTTPS dành cho Android, không gửi mật khẩu về lại ứng dụng và không đưa password hash ra ngoài máy chủ.
- Giữ nguyên lõi AccessibilityService, cử chỉ bắt buộc và target SDK 30 để không làm thay đổi đường chạy Android 10.

## 0.3.3 - Fallback focus chính xác hơn

- Nếu mục theo thứ tự màn hình không nhận accessibility focus, thử `focusSearch()` trước khi cuộn.
- Bỏ qua kết quả `focusSearch()` trỏ lại chính mục hiện tại để tránh vuốt nhưng không chuyển mục.
- Tiếp tục bảo đảm candidate được recycle đúng một lần dù đến từ danh sách sắp xếp hoặc fallback.

## 0.3.2 - Ổn định điều hướng đa ứng dụng

- Nhận diện lại mục đang focus ổn định hơn khi danh sách động thay đổi vị trí nhưng giữ nguyên view ID.
- Loại bớt container có mục con đã đọc được để vuốt trái/phải không dừng lặp ở các lớp giao diện trung gian.
- Gộp callback tự cuộn khi vuốt nhanh, tránh xếp chồng nhiều lần điều hướng sau một lần cuộn.
- Tự khôi phục và đọc lại accessibility focus sau khi ứng dụng đổi cửa sổ.

## 0.3.1 - Sửa tương thích tổng thể

- Giữ đúng quy ước bắt buộc: vuốt trái là mục trước, vuốt phải là mục tiếp theo; vuốt lên/xuống điều khiển cuộn.
- Bảo vệ việc tái kết nối service để không giữ lại ToneGenerator hoặc TTS cũ.
- Thêm nút đọc lại mục gần nhất, công tắc earcon và chế độ đọc ngắn gọn.
- Dọn trạng thái focus và callback khi service bị hủy để giảm lỗi khi đổi ứng dụng hoặc đổi cửa sổ.

## 0.3.0 - Củng cố lõi và kiểm tra cập nhật

- Bảo vệ vòng đời `AccessibilityNodeInfo`, tránh recycle trùng và recycle kết quả `focusSearch()` đúng cách.
- Củng cố xử lý đổi cửa sổ, focus accessibility, vùng cuộn và fallback sắp xếp theo vị trí màn hình.
- Thêm kiểm tra bản cập nhật ReaderHub qua HTTPS/JSON ở chế độ nền và nút mở liên kết APK khi có bản mới.
- Bổ sung quyền Internet tối thiểu và giữ máy chủ cập nhật có thể cấu hình bằng resource.
- Cải thiện mô tả cử chỉ: vuốt trái là mục trước, vuốt phải là mục tiếp theo.

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
