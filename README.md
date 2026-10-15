# Trolyao Macro — Android MVP

Dự án Kotlin + Jetpack Compose, có:
- Chọn ứng dụng đã cài (danh sách ứng dụng cục bộ).
- Lưu lựa chọn và danh sách lệnh macro trên thiết bị.
- Trình sửa lệnh: Click, MultiClick, Swipe, Delay, Long Click.
- Accessibility Service dùng `dispatchGesture` để phát thao tác chạm; người dùng phải tự bật dịch vụ trong Cài đặt Android.
- Overlay nổi khi dịch vụ trợ năng nhận thấy ứng dụng đã chọn đang ở trước màn hình.
- Nút nổi chạy/dừng macro và mở màn hình ứng dụng.

## Cài đặt / build
1. Mở thư mục `Trolyao-Full` bằng Android Studio.
2. Cài Android SDK 35 và JDK 17, chờ Gradle Sync.
3. Build > Build APK(s), hoặc Run để cài trực tiếp.
4. Mở app, chọn game, cấp quyền hiển thị trên ứng dụng khác.
5. Vào Cài đặt Android > Trợ năng > Trolyao Macro Service và bật thủ công.

## Giới hạn
- Đây là MVP mã nguồn, không kèm APK đã build và chưa được kiểm thử trên thiết bị của bạn.
- Android/nhà sản xuất có thể hạn chế overlay hoặc Accessibility Service; macro có thể không hoạt động trong một số game.
- Tọa độ là pixel tuyệt đối trên màn hình, nên khác nhau giữa thiết bị/độ phân giải/hướng màn hình.
- Không có chức năng né anti-cheat. Hãy dùng theo quy định của ứng dụng/game và tắt macro khi không cần.
- Android yêu cầu người dùng tự cấp quyền hệ thống; app không thể tự bật Shizuku, overlay hay Accessibility Service.
