package com.licham

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

data class VanKhan(val title: String, val body: String)

private val vanKhanList = listOf(
    VanKhan(
        "Văn khấn Thổ Công (ngày rằm, mùng 1)",
        "Nam mô A Di Đà Phật! (3 lần)\n\n" +
        "Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.\n\n" +
        "Con kính lạy Hoàng Thiên Hậu Thổ chư vị Tôn thần.\n" +
        "Con kính lạy ngài Đông Trù Tư Mệnh Táo Phủ Thần quân.\n" +
        "Con kính lạy các ngày Thần linh bản xứ cai quản trong xứ này.\n\n" +
        "Hôm nay là ngày... tháng... năm...\n" +
        "Tín chủ con là: ...\n" +
        "Ngụ tại: ...\n" +
        "Thành tâm sửa biện hương hoa, lễ vật, kim ngân trà quả, dâng lên trước án.\n\n" +
        "Chúng con kính mời ngài Đông Trù Tư Mệnh Táo Phủ Thần quân, Thổ Địa Long Mạch Tôn thần, các ngài Thần linh cai quản trong xứ này.\n" +
        "Cúi xin giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.\n\n" +
        "Tín chủ con lại kính mời các vị Tiền chủ, Hậu chủ ngụ tại nhà này, cùng về hâm hưởng.\n\n" +
        "Cúi xin các ngài phù hộ cho toàn gia chúng con: sức khỏe dồi dào, vạn sự tốt lành, mọi việc hanh thông.\n\n" +
        "Nam mô A Di Đà Phật! (3 lần)"
    ),
    VanKhan(
        "Văn khấn Gia Tiên (ngày rằm, mùng 1)",
        "Nam mô A Di Đà Phật! (3 lần)\n\n" +
        "Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.\n\n" +
        "Con kính lạy tổ tiên nội ngoại họ...\n\n" +
        "Hôm nay là ngày... tháng... năm...\n" +
        "Tín chủ con là: ...\n" +
        "Ngụ tại: ...\n\n" +
        "Nhân ngày lễ (rằm/mùng 1) tháng... năm..., chúng con thành tâm sửa biện hương hoa, lễ vật, kim ngân trà quả, dâng lên trước án.\n\n" +
        "Kính mời các cụ Cao Tằng Tổ Khảo, Cao Tằng Tổ Tỷ, Bá Thúc Huynh Đệ, Cô Di Tỷ Muội, nội ngoại họ...\n" +
        "Cúi xin thương xót con cháu, giáng về linh sàng, chứng giám lòng thành, thụ hưởng lễ vật.\n\n" +
        "Kính xin phù hộ cho con cháu: mạnh khỏe, bình an, làm ăn thuận lợi, gia đạo hưng long.\n\n" +
        "Nam mô A Di Đà Phật! (3 lần)"
    ),
    VanKhan(
        "Văn khấn Ông Táo (ngày 23 tháng Chạp)",
        "Nam mô A Di Đà Phật! (3 lần)\n\n" +
        "Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.\n\n" +
        "Con kính lạy ngài Đông Trù Tư Mệnh Táo Phủ Thần quân.\n\n" +
        "Hôm nay là ngày 23 tháng Chạp năm...\n" +
        "Tín chủ con là: ...\n" +
        "Ngụ tại: ...\n\n" +
        "Nhân ngày Táo Quân chầu trời, chúng con thành tâm sửa biện hương hoa, lễ vật, dâng lên trước án.\n\n" +
        "Kính mời ngài Đông Trù Tư Mệnh Táo Phủ Thần quân.\n" +
        "Cúi xin ngài giáng lâm trước án, chứng giám lòng thành, thụ hưởng lễ vật.\n\n" +
        "Cúi xin ngài tâu với Ngọc Hoàng Thượng Đế những điều phúc đức, tốt lành cho gia đình chúng con.\n" +
        "Xin ngài phù hộ cho toàn gia: sức khỏe dồi dào, mọi việc hanh thông.\n\n" +
        "Nam mô A Di Đà Phật! (3 lần)"
    ),
    VanKhan(
        "Văn khấn Thần Tài (ngày mùng 1 và ngày vía)",
        "Nam mô A Di Đà Phật! (3 lần)\n\n" +
        "Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.\n\n" +
        "Con kính lạy ngài Thần Tài, Thổ Địa, các ngài Thần linh cai quản trong xứ này.\n\n" +
        "Hôm nay là ngày... tháng... năm...\n" +
        "Tín chủ con là: ...\n" +
        "Ngụ tại: ...\n\n" +
        "Thành tâm sửa biện hương hoa, lễ vật, dâng lên trước án.\n\n" +
        "Kính mời ngài Thần Tài, Thổ Địa, các ngài Thần linh.\n" +
        "Cúi xin các ngài giáng lâm chứng giám, thụ hưởng lễ vật.\n\n" +
        "Cúi xin các ngài phù hộ cho gia đình chúng con: làm ăn phát đạt, buôn may bán đắt, tài lộc dồi dào, vạn sự hanh thông.\n\n" +
        "Nam mô A Di Đà Phật! (3 lần)"
    ),
    VanKhan(
        "Văn khấn Lễ Phật (ngày rằm, mùng 1)",
        "Nam mô A Di Đà Phật! (3 lần)\n\n" +
        "Con lạy chín phương Trời, mười phương Chư Phật, Chư Phật mười phương.\n\n" +
        "Hôm nay là ngày... tháng... năm...\n" +
        "Tín chủ con là: ...\n" +
        "Ngụ tại: ...\n\n" +
        "Thành tâm dâng lên Phật đài hương hoa, lễ vật, trà quả.\n\n" +
        "Con xin phát nguyện tu hành: giữ gìn giới luật, làm lành lánh dữ.\n" +
        "Cúi xin Chư Phật từ bi gia hộ cho con và gia đình: thân tâm an lạc, trí tuệ sáng suốt, nghiệp chướng tiêu trừ.\n\n" +
        "Nam mô A Di Đà Phật! (3 lần)"
    )
)

@Composable
fun VanKhanScreen() {
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing16, vertical = Spacing12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(Spacing12))
                Text(
                    text = "Văn khấn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (selectedIndex >= 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing16)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = Spacing1)
                ) {
                    Column(modifier = Modifier.padding(Spacing16)) {
                        Button(
                            onClick = { selectedIndex = -1 },
                            modifier = Modifier.padding(bottom = Spacing12),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text("\u2190 Danh sách", style = MaterialTheme.typography.labelLarge)
                        }
                        Text(
                            text = vanKhanList[selectedIndex].title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing12))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(Spacing12))
                        Text(
                            text = vanKhanList[selectedIndex].body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing16),
                verticalArrangement = Arrangement.spacedBy(Spacing12)
            ) {
                Text(
                    text = "Chọn bài văn khấn để đọc",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing8)
                )
                vanKhanList.forEachIndexed { index, vk ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Spacing1),
                        onClick = { selectedIndex = index }
                    ) {
                        Text(
                            text = vk.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(Spacing16)
                        )
                    }
                }
            }
        }
    }
}
