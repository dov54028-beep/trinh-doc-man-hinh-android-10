# Trình đọc màn hình Android 10 độc lập

Đây là phiên bản thử nghiệm đầu tiên của một trình đọc màn hình viết mới bằng Java, không dùng lõi TalkBack. Mục tiêu là kiểm soát rõ ràng luồng khởi động, màn hình cài đặt và tương thích Android 10.

## Phiên bản 0.2.0

Bản 0.2 cung cấp `AccessibilityService`, TTS hệ thống, đọc nhãn và trạng thái cơ bản của mục giao diện, điều hướng trái/phải, cuộn lên/xuống, chạm khám phá, nhấn hai lần để gọi nút đang được khám phá, và phản hồi earcon cho focus, cử chỉ vuốt, kích hoạt và lỗi. Earcon dùng `ToneGenerator` của Android nên không cần tải thêm tệp âm thanh.

Không nên coi bản 0.1.0 là bản thay thế hoàn chỉnh TalkBack. Trình đọc màn hình hoàn chỉnh cần xử lý rất nhiều loại cây giao diện và thiết bị.

## Cử chỉ và phản hồi

Khi dịch vụ được bật, vuốt trái/phải sẽ chuyển accessibility focus giữa các mục. Vuốt lên/xuống sẽ yêu cầu cuộn vùng đang focus. Đưa ngón tay qua mục sẽ đọc nhãn và phát earcon focus; nhấn hai lần trên mục đang được khám phá sẽ gọi `ACTION_CLICK` và phát earcon kích hoạt.

Đây vẫn là bản độc lập đang phát triển. Một số ứng dụng tùy biến hoặc canvas không cung cấp đủ `AccessibilityNodeInfo`, vì vậy có thể chưa hỗ trợ tốt như TalkBack.

## Cài đặt và bật

1. Cài APK trên Android 10.
2. Mở biểu tượng **Trình đọc màn hình Android 10**.
3. Nhấn **Mở cài đặt Trình đọc màn hình** để xem cài đặt riêng.
4. Vào **Cài đặt Android → Trợ năng → Ứng dụng đã cài đặt** và bật dịch vụ.
5. Quay lại ứng dụng để kiểm tra TTS và trạng thái dịch vụ.

## Build

GitHub Actions build `:app:assembleDebug`. Không commit thư mục `build/`, cache hoặc APK vào source.
