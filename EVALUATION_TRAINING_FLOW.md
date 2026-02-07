# 📘 HỆ THỐNG ĐÁNH GIÁ & ĐÀO TẠO - FLOW HOÀN CHỈNH

## 🎯 TỔNG QUAN

Module Evaluation & Training đã được bổ sung với các tính năng:
1. **Xếp hạng hiệu suất tự động** (Performance Ranking)
2. **Đề xuất đào tạo tự động** dựa trên KPI yếu
3. **Evidence submission workflow** - Nhân viên phải nộp chứng chỉ sau khi hoàn thành khóa học

---

## 📋 FLOW 1: ĐÁNH GIÁ & TỰ ĐỘNG ĐỀ XUẤT ĐÀO TẠO

### **Bước 1: Employee tự đánh giá**
```http
POST /api/evaluation/create
{
  "empId": 1,
  "cycleId": 1
}
```
→ Tạo evaluation với status = `SELF_REVIEW`
→ Tự động tạo `EvaluationDetail` cho mỗi KPI được gán

### **Bước 2: Employee nhập điểm tự đánh giá**
```http
PUT /api/evaluation/{evalId}/self-score
{
  "kpiId": 1,
  "selfScore": 75
}
```
→ Nhập điểm cho từng KPI

### **Bước 3: Employee upload minh chứng (Evidence)**
```http
POST /api/evaluation/{evalId}/evidence
{
  "kpiId": 1,
  "fileUrl": "https://example.com/evidence.pdf",
  "description": "Báo cáo dự án Q1"
}
```
→ HR có thể verify evidence

### **Bước 4: Employee submit đánh giá**
```http
PUT /api/evaluation/{evalId}/submit
{
  "comment": "Em đã hoàn thành tốt các công việc"
}
```
→ Status: `SELF_REVIEW` → `MANAGER_REVIEW`

### **Bước 5: Manager đánh giá**
```http
PUT /api/evaluation/{evalId}/manager-score
{
  "kpiId": 1,
  "managerScore": 70
}
```
→ Manager nhập điểm cho từng KPI

### **Bước 6: Manager approve đánh giá**
```http
PUT /api/evaluation/{evalId}/approve
{
  "managerComment": "Hoàn thành tốt nhiệm vụ"
}
```
→ Hệ thống tự động:
  - Tính điểm tổng (`finalScore`)
  - Phân loại (`classification`): A, B, C, D
  - Status: `MANAGER_REVIEW` → `COMPLETED`

### **Bước 7: 🤖 HỆ THỐNG TỰ ĐỘNG TẠO TRAINING RECOMMENDATION**
```http
POST /api/performance-ranking/auto-recommend/{evalId}
```

**Logic tự động:**

#### **Classification D hoặc C (Yếu):**
1. Tìm KPI có `finalScore < 60`
2. Match với `training_programs` có `skill_category` tương ứng
3. Tạo `TrainingRecommendation`:
   - `priority` = `HIGH`
   - `status` = `PENDING`
   - `reason` = "KPI 'Communication' chỉ đạt 45 điểm. Cần đào tạo kỹ năng giao tiếp."
4. **Notify Manager**: "Nhân viên X đạt điểm C. Hệ thống đề xuất 3 khóa đào tạo. Vui lòng review."

#### **Classification B (Trung bình):**
1. Tạo recommendation với `priority` = `MEDIUM`
2. **Notify Employee**: "Bạn có 5 khóa học gợi ý, hãy chọn khóa phù hợp"

#### **Classification A (Xuất sắc):**
- Không tạo recommendation
- Employee tự đăng ký nếu muốn

### **Bước 8: Manager xem recommendation và GÁN TRAINING**

#### **Xem recommendation:**
```http
GET /api/training/recommendation/employee/{empId}
```

#### **Manager GÁN KHÓA HỌC BẮT BUỘC (với nhân viên yếu):**
```http
POST /api/training/assign
{
  "empId": 1,
  "programId": 5,
  "assignedBy": 10, // Manager ID
  "objective": "Cải thiện kỹ năng giao tiếp"
}
```
→ Tạo `TrainingAssignment` với status = `PLANNED`
→ Tự động tạo `TrainingProgress` với status = `NOT_STARTED`

#### **Hoặc GÁN MENTOR 1-1 (cho trường hợp nghiêm trọng):**
```http
POST /api/training/assign/mentor
{
  "empId": 1,
  "mentorId": 15, // Senior ID
  "assignedBy": 10,
  "objective": "Mentoring về technical skills"
}
```

---

## 📋 FLOW 2: EVIDENCE SUBMISSION SAU KHI HOÀN THÀNH KHÓA HỌC

### **Bước 1: Employee bắt đầu học**
```http
PUT /api/training/progress/{progressId}
{
  "completionPercentage": 10,
  "status": "IN_PROGRESS"
}
```
→ Status: `NOT_STARTED` → `IN_PROGRESS`

### **Bước 2: Employee cập nhật tiến độ**
```http
PUT /api/training/progress/{progressId}
{
  "completionPercentage": 50,
  "status": "IN_PROGRESS"
}
```

### **Bước 3: 🎓 Employee HOÀN THÀNH khóa học**
```http
PUT /api/training/progress/{progressId}/complete
```
→ Status: `IN_PROGRESS` → `AWAITING_EVIDENCE`
→ Hệ thống gửi notification: **"Vui lòng upload chứng chỉ hoàn thành"**

### **Bước 4: 📄 Employee UPLOAD CHỨNG CHỈ**
```http
POST /api/training/certificate
{
  "empId": 1,
  "programId": 5,
  "certificateName": "Certificate of Completion - Communication Skills",
  "fileUrl": "https://storage.example.com/cert_123.pdf"
}
```
→ Tạo `TrainingCertificate` với status = `PENDING_VERIFICATION`
→ Notify Manager: **"Nhân viên X đã upload chứng chỉ, cần duyệt"**

### **Bước 5: Manager XEM chứng chỉ chờ duyệt**
```http
GET /api/training/certificate/pending
```
→ Trả về list các certificate có status = `PENDING_VERIFICATION`

### **Bước 6a: ✅ Manager APPROVE chứng chỉ**
```http
PUT /api/training/certificate/{certId}/verify
{
  "isValid": true,
  "verifiedBy": 10,
  "verificationNote": "Chứng chỉ hợp lệ, đã hoàn thành đầy đủ"
}
```
→ Certificate status: `PENDING_VERIFICATION` → `VERIFIED`
→ **TrainingProgress status: `AWAITING_EVIDENCE` → `COMPLETED`** ✅
→ **TrainingAssignment status: → `COMPLETED`**

### **Bước 6b: ❌ Manager REJECT chứng chỉ**
```http
PUT /api/training/certificate/{certId}/verify
{
  "isValid": false,
  "verifiedBy": 10,
  "verificationNote": "Chứng chỉ không rõ ràng, vui lòng upload lại"
}
```
→ Certificate status: `PENDING_VERIFICATION` → `REJECTED`
→ TrainingProgress status: vẫn là `AWAITING_EVIDENCE`
→ Notify Employee: **"Chứng chỉ bị từ chối. Vui lòng upload lại"**
→ Employee phải upload lại (quay lại Bước 4)

---

## 📋 FLOW 3: XẾP HẠNG HIỆU SUẤT & THĂNG CHỨC

### **Sau khi KẾT THÚC CYCLE đánh giá**

#### **Bước 1: HR tính ranking cho toàn bộ cycle**
```http
POST /api/performance-ranking/calculate/{cycleId}
```

**Hệ thống tự động:**
1. Lấy tất cả evaluation có status = `COMPLETED` trong cycle
2. Sắp xếp theo `final_score` giảm dần
3. Tính rank cho từng nhân viên:
   - `rank_overall`: Rank trong toàn công ty
   - `rank_in_dept`: Rank trong phòng ban
   - `percentile`: Phần trăm vượt trội
4. Xác định đủ điều kiện thăng chức:
   - **Rule 1**: Classification = A **VÀ** top 10% → `is_promotion_eligible = true`
   - **Rule 2**: Classification = A **VÀ** rank ≤ 5 → `is_promotion_eligible = true`
5. Đánh dấu cần đào tạo:
   - Classification = C hoặc D → `is_training_required = true`

#### **Bước 2: HR xem danh sách ứng viên thăng chức**
```http
GET /api/performance-ranking/cycle/{cycleId}/promotion-candidates
```
→ Trả về list nhân viên có `is_promotion_eligible = true`

#### **Bước 3: HR xem top performers**
```http
GET /api/performance-ranking/cycle/{cycleId}/top?limit=10
```
→ Trả về top 10 nhân viên xuất sắc nhất

#### **Bước 4: Employee xem ranking của mình**
```http
GET /api/performance-ranking/employee/{empId}/cycle/{cycleId}
```
→ Trả về thông tin:
- `finalScore`: 85.5
- `rankOverall`: 15 / 200
- `rankInDept`: 3 / 25
- `percentile`: 92.5
- `classification`: A
- `isPromotionEligible`: true
- `rewardRecommendation`: "Xuất sắc, nằm trong top 10%. Đề xuất thăng chức."

---

## 🔄 FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────┐
│ 1. EMPLOYEE TỰ ĐÁNH GIÁ                                │
│    - Nhập self_score cho từng KPI                      │
│    - Upload evidence                                    │
│    - Submit → MANAGER_REVIEW                           │
└──────────────────┬──────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────────────────────┐
│ 2. MANAGER ĐÁNH GIÁ                                     │
│    - Nhập manager_score cho từng KPI                   │
│    - Approve → COMPLETED                               │
└──────────────────┬──────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────────────────────┐
│ 3. HỆ THỐNG TỰ ĐỘNG PHÂN LOẠI                          │
│    - Tính final_score (theo weight)                    │
│    - Classification: A / B / C / D                     │
└──────────────────┬──────────────────────────────────────┘
                   ↓
         ┌─────────┴─────────┬─────────────┐
         ↓                   ↓             ↓
    ┌─────────┐         ┌─────────┐   ┌─────────┐
    │ A (Giỏi)│         │ B (TB)  │   │ C/D (Yếu)│
    └────┬────┘         └────┬────┘   └────┬─────┘
         │                   │             │
         ↓                   ↓             ↓
┌──────────────┐    ┌──────────────┐  ┌──────────────────┐
│ Xem xét      │    │ Gợi ý khóa   │  │ TỰ ĐỘNG TẠO      │
│ THĂNG CHỨC   │    │ học (MEDIUM) │  │ RECOMMENDATION   │
│              │    │              │  │ Priority: HIGH   │
│              │    │ Employee tự  │  │                  │
│              │    │ đăng ký      │  │ Notify Manager   │
└──────────────┘    └──────────────┘  └────────┬─────────┘
                                               ↓
                                    ┌─────────────────────┐
                                    │ MANAGER REVIEW      │
                                    │ & GÁN TRAINING      │
                                    │ - Khóa học bắt buộc │
                                    │ - Hoặc Mentor 1-1   │
                                    └─────────┬───────────┘
                                              ↓
┌─────────────────────────────────────────────────────────┐
│ 4. EMPLOYEE HỌC VÀ HOÀN THÀNH                           │
│    - Status: IN_PROGRESS                               │
│    - Báo hoàn thành → AWAITING_EVIDENCE                │
└──────────────────┬──────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────────────────────┐
│ 5. EMPLOYEE UPLOAD CHỨNG CHỈ                            │
│    - Upload certificate file                           │
│    - Status: PENDING_VERIFICATION                      │
│    - Notify Manager                                    │
└──────────────────┬──────────────────────────────────────┘
                   ↓
         ┌─────────┴─────────┐
         ↓                   ↓
    ┌─────────┐         ┌─────────┐
    │ APPROVE │         │ REJECT  │
    └────┬────┘         └────┬────┘
         ↓                   ↓
┌──────────────┐    ┌──────────────┐
│ ✅ COMPLETED │    │ Upload lại   │
└──────────────┘    └──────────────┘
```

---

## 📊 DATABASE FLOW

### **Tables liên quan:**

1. **employee_evaluations** (Evaluation)
   - Lưu thông tin đánh giá tổng thể
   - Fields: `final_score`, `classification`, `status`

2. **evaluation_kpi_scores** (EvaluationDetail)
   - Lưu điểm từng KPI
   - Fields: `self_score`, `manager_score`, `final_score`

3. **evaluation_evidence** (EvaluationEvidence)
   - Lưu minh chứng KPI
   - Fields: `file_url`, `verification_status`

4. **performance_rankings** (PerformanceRanking)
   - Lưu ranking và đề xuất thăng chức
   - Fields: `rank_overall`, `is_promotion_eligible`, `is_training_required`

5. **training_recommendations** (TrainingRecommendation)
   - Lưu gợi ý đào tạo tự động
   - Fields: `priority`, `reason`, `status`

6. **training_assignments** (TrainingAssignment)
   - Lưu việc gán khóa học/mentor
   - Fields: `training_type`, `status`

7. **training_progress** (TrainingProgress)
   - Theo dõi tiến độ học
   - Fields: `completion_percentage`, `status` (**AWAITING_EVIDENCE**)

8. **training_certificates** (TrainingCertificate)
   - Lưu chứng chỉ hoàn thành
   - Fields: `file_url`, `status` (`PENDING_VERIFICATION`, `VERIFIED`, `REJECTED`)

---

## ✅ CHECKLIST TÍNH NĂNG ĐÃ HOÀN THÀNH

- ✅ Đánh giá nhân viên theo KPI (với trọng số)
- ✅ Tính điểm tự động và phân loại A/B/C/D
- ✅ Xếp hạng hiệu suất (rank overall, rank in dept, percentile)
- ✅ **Đánh giá tự động thăng chức** (`is_promotion_eligible`)
- ✅ **Tự động tạo recommendation** dựa trên KPI yếu
- ✅ Manager gán khóa học hoặc mentor cho nhân viên yếu
- ✅ Theo dõi tiến độ đào tạo
- ✅ **Evidence submission workflow** (AWAITING_EVIDENCE)
- ✅ Manager verify certificate
- ✅ Lưu lịch sử đánh giá

---

## 🎯 USE CASES

### **Use Case 1: Nhân viên YẾU (Classification D)**
1. Manager approve evaluation → Classification = D
2. Gọi API auto-recommend → Tạo 3 recommendation (priority: HIGH)
3. Manager nhận notification
4. Manager GÁN MENTOR 1-1 cho nhân viên
5. Nhân viên học với mentor 3 tháng
6. Sau 3 tháng: Nhân viên báo hoàn thành → Upload evidence
7. Manager verify → COMPLETED

### **Use Case 2: Nhân viên TRUNG BÌNH (Classification B)**
1. Manager approve evaluation → Classification = B
2. Gọi API auto-recommend → Tạo 5 recommendation (priority: MEDIUM)
3. Employee nhận notification: "5 khóa học gợi ý"
4. Employee TỰ CHỌN 2 khóa học
5. Manager duyệt
6. Employee học xong → Upload certificate → Manager verify

### **Use Case 3: Nhân viên XUẤT SẮC (Classification A)**
1. Manager approve evaluation → Classification = A
2. Gọi API calculate-ranking → `is_promotion_eligible = true`
3. HR xem promotion candidates
4. HR đề xuất thăng chức cho Ban Giám Đốc

---

## 🔧 API SUMMARY

### **Evaluation APIs:**
- `POST /api/evaluation/create` - Tạo evaluation
- `PUT /api/evaluation/{evalId}/self-score` - Employee nhập điểm
- `PUT /api/evaluation/{evalId}/submit` - Submit đánh giá
- `PUT /api/evaluation/{evalId}/approve` - Manager approve
- `POST /api/evaluation/{evalId}/evidence` - Upload evidence KPI

### **Performance Ranking APIs:**
- `POST /api/performance-ranking/calculate/{cycleId}` - Tính ranking
- `POST /api/performance-ranking/auto-recommend/{evalId}` - Tự động tạo recommendation
- `GET /api/performance-ranking/cycle/{cycleId}/promotion-candidates` - Ứng viên thăng chức
- `GET /api/performance-ranking/cycle/{cycleId}/top?limit=10` - Top performers

### **Training APIs:**
- `POST /api/training/assign` - Gán khóa học
- `POST /api/training/assign/mentor` - Gán mentor 1-1
- `PUT /api/training/progress/{progressId}/complete` - Báo hoàn thành
- `POST /api/training/certificate` - Upload chứng chỉ
- `PUT /api/training/certificate/{certId}/verify` - Verify chứng chỉ
- `GET /api/training/certificate/pending` - List chứng chỉ chờ duyệt

---

## 💡 NOTES

1. **Notification**: Các TODO comment trong code đánh dấu nơi cần tích hợp notification service
2. **Security**: Cần thêm authentication và authorization checks
3. **File Upload**: API nhận `fileUrl` - cần implement file upload service riêng
4. **Email**: Có thể tích hợp email notification khi có recommendation mới

---

Đã hoàn thành module Evaluation & Training với đầy đủ tính năng! 🎉
