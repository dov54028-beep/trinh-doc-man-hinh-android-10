# Trình đọc màn hình Android 10 độc lập

Đây là phiên bản thử nghiệm đầu tiên của một trình đọc màn hình viết mới bằng Java, không dùng lõi TalkBack. Mục tiêu là kiểm soát rõ ràng luồng khởi động, màn hình cài đặt và tương thích Android 10.

## Phiên bản 0.4.2

Bản 0.4.2 tiếp tục các sửa ổn định của 0.3.3, tích hợp màn hình tài khoản ReaderHub ngay trong APK và củng cố lõi cử chỉ. Vuốt trái vẫn là mục trước, vuốt phải là mục tiếp theo; callback lặp bị chống, node động được nhận diện thận trọng hơn, nhãn con trùng trong card được giảm bớt và vai trò điều khiển được đọc rõ hơn. Người dùng có thể đăng ký, đăng nhập, đăng xuất, xem mã thiết bị và mở bảng điều khiển đầy đủ để dùng tài khoản, quản trị, chat, hiện diện online, cấm thiết bị, audit log và reset token.

Không nên coi bản 0.1.0 là bản thay thế hoàn chỉnh TalkBack. Trình đọc màn hình hoàn chỉnh cần xử lý rất nhiều loại cây giao diện và thiết bị.

## Cử chỉ và phản hồi

Khi dịch vụ được bật, vuốt trái/phải sẽ chuyển accessibility focus giữa các mục. Vuốt lên/xuống sẽ yêu cầu cuộn vùng đang focus. Đưa ngón tay qua mục sẽ đọc nhãn và phát earcon focus; nhấn hai lần trên mục đang được khám phá sẽ gọi `ACTION_CLICK` và phát earcon kích hoạt.

Vuốt trái là mục trước; vuốt phải là mục tiếp theo. Vuốt lên yêu cầu cuộn xuống và vuốt xuống yêu cầu cuộn lên. Khi `focusSearch()` của ứng dụng không hoạt động, service thu thập mục nhìn thấy, sắp xếp theo tọa độ trên màn hình và dùng đó làm đường dự phòng.

Đây vẫn là bản độc lập đang phát triển. Ứng dụng tùy biến, game canvas hoặc giao diện không cung cấp `AccessibilityNodeInfo` có thể cần bộ thích nghi riêng.

## Tương thích Android 10 và Android mới

- `minSdk 26`: Android 8.0 trở lên, bao gồm Android 10.
- `compileSdk 36`: mã nguồn được biên dịch với bộ SDK mới để có thể kiểm tra Android hiện đại.
- `targetSdk 30`: giữ hành vi ổn định cho Android 10 trong giai đoạn lõi cử chỉ còn đang hoàn thiện; không tự ý nâng target chỉ vì compile SDK mới hơn.
- Mã nguồn Java tránh gọi API mới trực tiếp trong đường chạy Android 10. Khi bổ sung API mới, cần đặt kiểm tra `Build.VERSION.SDK_INT` và giữ đường fallback cũ.
- Giao diện trợ năng dựa trên API chuẩn `AccessibilityService` và `AccessibilityNodeInfo`, không phụ thuộc lõi TalkBack, nên cùng một APK có thể chạy trên Android 10 và Android mới trong giới hạn node mà từng ứng dụng cung cấp.

Repository đã đặt công khai để người dùng không cần đăng nhập GitHub khi kiểm tra cập nhật hoặc tải APK. Release 0.4.2 đã được liên kết vào ReaderHub; update checker có thể lấy metadata và APK công khai mà không cần đăng nhập GitHub.

## Ý tưởng đã tự chọn cho các bản sau

Các hướng mở rộng hợp lý là bộ điều hướng theo vai trò (nút, tiêu đề, liên kết, ô nhập), đọc trạng thái tiến độ và thông báo ngắn, lịch sử focus để quay lại mục vừa đọc, chế độ đọc liên tục có dừng bằng chạm, bộ lọc thông báo theo ứng dụng, thao tác tùy biến trong màn hình cài đặt và báo cáo lỗi ẩn danh chỉ khi người dùng chủ động bật. Những tính năng này sẽ được thêm sau khi nền cử chỉ ổn định; không coi chúng là đã hoàn thành trong 0.4.2. Khả năng tương thích rộng vẫn phụ thuộc vào việc từng ứng dụng cung cấp cây trợ năng hợp lệ và cần được xác nhận thêm trên thiết bị Android thật.

## Cài đặt và bật

1. Cài APK trên Android 10.
2. Mở biểu tượng **Trình đọc màn hình Android 10**.
3. Nhấn **Mở cài đặt Trình đọc màn hình** để xem cài đặt riêng.
4. Vào **Cài đặt Android → Trợ năng → Ứng dụng đã cài đặt** và bật dịch vụ.
5. Trong cài đặt riêng, có thể tắt earcon hoặc bật đọc ngắn gọn; nút **Đọc lại mục hiện tại** ở màn hình chính đọc lại nội dung gần nhất.
6. Quay lại ứng dụng để kiểm tra TTS và trạng thái dịch vụ.

## Build

GitHub Actions build `:app:assembleDebug`. Không commit thư mục `build/`, cache hoặc APK vào source.
