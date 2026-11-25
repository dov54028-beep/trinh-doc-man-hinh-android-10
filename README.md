# Trình đọc màn hình Android 10 độc lập

Đây là phiên bản thử nghiệm đầu tiên của một trình đọc màn hình viết mới bằng Java, không dùng lõi TalkBack. Mục tiêu là kiểm soát rõ ràng luồng khởi động, màn hình cài đặt và tương thích Android 10.

## Phiên bản 0.3.1

Bản 0.3.1 tiếp tục lõi độc lập, củng cố vòng đời node cho Android 10, bảo vệ recycle node, bổ sung fallback điều hướng theo vị trí màn hình, xử lý biên danh sách, kiểm tra cập nhật online, nút đọc lại mục gần nhất, công tắc earcon và chế độ đọc ngắn gọn. Bản 0.2 cung cấp TTS hệ thống, đọc nhãn/trạng thái, điều hướng trái/phải, cuộn lên/xuống, chạm khám phá, nhấn hai lần và earcon.

Không nên coi bản 0.1.0 là bản thay thế hoàn chỉnh TalkBack. Trình đọc màn hình hoàn chỉnh cần xử lý rất nhiều loại cây giao diện và thiết bị.

## Cử chỉ và phản hồi

Khi dịch vụ được bật, vuốt trái/phải sẽ chuyển accessibility focus giữa các mục. Vuốt lên/xuống sẽ yêu cầu cuộn vùng đang focus. Đưa ngón tay qua mục sẽ đọc nhãn và phát earcon focus; nhấn hai lần trên mục đang được khám phá sẽ gọi `ACTION_CLICK` và phát earcon kích hoạt.

Vuốt trái là mục trước; vuốt phải là mục tiếp theo. Vuốt lên yêu cầu cuộn xuống và vuốt xuống yêu cầu cuộn lên. Khi `focusSearch()` của ứng dụng không hoạt động, service thu thập mục nhìn thấy, sắp xếp theo tọa độ trên màn hình và dùng đó làm đường dự phòng.

Đây vẫn là bản độc lập đang phát triển. Ứng dụng tùy biến, game canvas hoặc giao diện không cung cấp `AccessibilityNodeInfo` có thể cần bộ thích nghi riêng.

## Ý tưởng đã tự chọn cho các bản sau

Các hướng mở rộng hợp lý là bộ điều hướng theo vai trò (nút, tiêu đề, liên kết, ô nhập), đọc trạng thái tiến độ và thông báo ngắn, lịch sử focus để quay lại mục vừa đọc, chế độ đọc liên tục có dừng bằng chạm, bộ lọc thông báo theo ứng dụng, thao tác tùy biến trong màn hình cài đặt và báo cáo lỗi ẩn danh chỉ khi người dùng chủ động bật. Những tính năng này sẽ được thêm sau khi nền cử chỉ ổn định; không coi chúng là đã hoàn thành trong 0.3.

## Cài đặt và bật

1. Cài APK trên Android 10.
2. Mở biểu tượng **Trình đọc màn hình Android 10**.
3. Nhấn **Mở cài đặt Trình đọc màn hình** để xem cài đặt riêng.
4. Vào **Cài đặt Android → Trợ năng → Ứng dụng đã cài đặt** và bật dịch vụ.
5. Trong cài đặt riêng, có thể tắt earcon hoặc bật đọc ngắn gọn; nút **Đọc lại mục hiện tại** ở màn hình chính đọc lại nội dung gần nhất.
6. Quay lại ứng dụng để kiểm tra TTS và trạng thái dịch vụ.

## Build

GitHub Actions build `:app:assembleDebug`. Không commit thư mục `build/`, cache hoặc APK vào source.
