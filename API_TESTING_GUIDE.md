# 🧪 HƯỚNG DẪN TEST API VÀ SỬ DỤNG SWAGGER

## 🚀 BƯỚC 1: CHẠY SERVER

### **Cách 1: Chạy từ IDE (IntelliJ IDEA / Eclipse)**
1. Mở file `HrmApplication.java`
2. Click chuột phải → **Run 'HrmApplication'**
3. Đợi console hiển thị:
   ```
   Started HrmApplication in 5.123 seconds
   ```

### **Cách 2: Chạy từ Command Line**
```bash
cd C:\Users\hoang\IdeaProjects\HRM-

# Maven
mvnw spring-boot:run

# Hoặc nếu đã build
java -jar target/hrm-0.0.1-SNAPSHOT.war
```

### **Kiểm tra server đã chạy:**
- Server chạy tại: `http://localhost:8080`
- Nếu thấy log `Tomcat started on port(s): 8080` → OK ✅

---

## 📚 BƯỚC 2: MỞ SWAGGER UI

### **Truy cập Swagger UI:**
```
http://localhost:8080/swagger-ui/index.html
```

Hoặc:
```
http://localhost:8080/swagger-ui.html
```

### **Bạn sẽ thấy giao diện:**
```
┌─────────────────────────────────────────────────────────┐
│ HRM System API - Evaluation & Training Module v1.0.0   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 📁 evaluation-controller                                │
│   ├── POST   /api/evaluation/create                    │
│   ├── PUT    /api/evaluation/{evalId}/submit           │
│   ├── PUT    /api/evaluation/{evalId}/approve          │
│   └── ...                                               │
│                                                         │
│ 📁 kpi-controller                                       │
│   ├── GET    /api/kpi/templates                        │
│   └── ...                                               │
│                                                         │
│ 📁 training-controller                                  │
│   ├── POST   /api/training/assign                      │
│   ├── POST   /api/training/certificate                 │
│   └── ...                                               │
│                                                         │
│ 📁 performance-ranking-controller                       │
│   ├── POST   /api/performance-ranking/calculate/{cycleId} │
│   ├── POST   /api/performance-ranking/auto-recommend/{evalId} │
│   └── ...                                               │
└─────────────────────────────────────────────────────────┘
```

---

## 🧪 BƯỚC 3: TEST API TRÊN SWAGGER

### **Cách test một API:**

#### **Ví dụ: Test API tạo evaluation**

1. **Tìm API trong Swagger UI:**
   - Mở section `evaluation-controller`
   - Click vào `POST /api/evaluation/create`

2. **Click nút "Try it out"**

3. **Nhập dữ liệu vào Request Body:**
   ```json
   {
     "empId": 1,
     "cycleId": 1
   }
   ```

4. **Click "Execute"**

5. **Xem kết quả:**
   - **Response Code**: `201 Created` (thành công)
   - **Response Body**: Object evaluation vừa tạo
     ```json
     {
       "evalId": 1,
       "empId": 1,
       "cycleId": 1,
       "status": "SELF_REVIEW",
       "selfScore": null,
       "managerScore": null,
       "finalScore": null,
       "classification": null
     }
     ```

---

## 🔥 TEST CASES QUAN TRỌNG

### **TEST CASE 1: FLOW ĐÁNH GIÁ CƠ BẢN**

#### **1.1. Tạo Evaluation**
```http
POST /api/evaluation/create
{
  "empId": 1,
  "cycleId": 1
}
```
→ Nhận `evalId` = 1

#### **1.2. Employee nhập điểm tự đánh giá**
```http
PUT /api/evaluation/1/self-score
{
  "kpiId": 1,
  "selfScore": 75
}
```

#### **1.3. Employee submit đánh giá**
```http
PUT /api/evaluation/1/submit
{
  "comment": "Em đã hoàn thành tốt"
}
```
→ Status: `SELF_REVIEW` → `MANAGER_REVIEW`

#### **1.4. Manager nhập điểm**
```http
PUT /api/evaluation/1/manager-score
{
  "kpiId": 1,
  "managerScore": 70
}
```

#### **1.5. Manager approve**
```http
PUT /api/evaluation/1/approve
{
  "managerComment": "Hoàn thành tốt"
}
```
→ Hệ thống tự động tính `finalScore`, `classification`

---

### **TEST CASE 2: TỰ ĐỘNG TẠO RECOMMENDATION (TÍNH NĂNG MỚI)**

#### **2.1. Sau khi Manager approve (ở trên), gọi auto-recommend:**
```http
POST /api/performance-ranking/auto-recommend/1
```

**Response:**
```json
[
  {
    "recommendationId": 1,
    "empId": 1,
    "evalId": 1,
    "programId": 5,
    "reason": "KPI 'Communication Skills' chỉ đạt 45 điểm. Cần đào tạo kỹ năng giao tiếp.",
    "priority": "HIGH",
    "status": "PENDING"
  },
  {
    "recommendationId": 2,
    "empId": 1,
    "evalId": 1,
    "programId": 8,
    "reason": "KPI 'Technical - Java' chỉ đạt 50 điểm. Cần đào tạo kỹ năng: Java Programming",
    "priority": "HIGH",
    "status": "PENDING"
  }
]
```

---

### **TEST CASE 3: EVIDENCE SUBMISSION WORKFLOW**

#### **3.1. Manager gán khóa học cho employee**
```http
POST /api/training/assign
{
  "empId": 1,
  "programId": 5,
  "assignedBy": 10,
  "objective": "Cải thiện kỹ năng giao tiếp"
}
```
→ Tạo `TrainingAssignment`, `TrainingProgress` (status = `NOT_STARTED`)

#### **3.2. Employee bắt đầu học**
```http
PUT /api/training/progress/1
{
  "completionPercentage": 50,
  "status": "IN_PROGRESS"
}
```

#### **3.3. Employee báo hoàn thành**
```http
PUT /api/training/progress/1/complete
```
→ Status: `IN_PROGRESS` → `AWAITING_EVIDENCE`
→ Hệ thống yêu cầu upload chứng chỉ

#### **3.4. Employee upload chứng chỉ**
```http
POST /api/training/certificate
{
  "empId": 1,
  "programId": 5,
  "certificateName": "Certificate of Completion",
  "fileUrl": "https://example.com/cert.pdf"
}
```
→ Certificate status = `PENDING_VERIFICATION`

#### **3.5. Manager verify chứng chỉ**
```http
PUT /api/training/certificate/1/verify
{
  "isValid": true,
  "verifiedBy": 10,
  "verificationNote": "Chứng chỉ hợp lệ"
}
```
→ Certificate status = `VERIFIED`
→ **TrainingProgress status = `COMPLETED`** ✅

---

### **TEST CASE 4: PERFORMANCE RANKING & PROMOTION**

#### **4.1. HR tính ranking cho cycle**
```http
POST /api/performance-ranking/calculate/1
```
→ Hệ thống tự động:
- Tính rank cho tất cả nhân viên
- Xác định `is_promotion_eligible`
- Đánh dấu `is_training_required`

#### **4.2. HR xem ứng viên thăng chức**
```http
GET /api/performance-ranking/cycle/1/promotion-candidates
```

Response:
```json
[
  {
    "rankId": 1,
    "empId": 5,
    "finalScore": 92.5,
    "rankOverall": 2,
    "rankInDept": 1,
    "classification": "A",
    "isPromotionEligible": true,
    "rewardRecommendation": "Xuất sắc, nằm trong top 10%. Đề xuất thăng chức."
  }
]
```

#### **4.3. Employee xem ranking của mình**
```http
GET /api/performance-ranking/employee/1/cycle/1
```

---

## 🎯 CHECKLIST TEST ĐẦY ĐỦ

### ✅ **Evaluation APIs:**
- [ ] POST `/api/evaluation/create`
- [ ] PUT `/api/evaluation/{evalId}/self-score`
- [ ] PUT `/api/evaluation/{evalId}/submit`
- [ ] PUT `/api/evaluation/{evalId}/manager-score`
- [ ] PUT `/api/evaluation/{evalId}/approve`
- [ ] PUT `/api/evaluation/{evalId}/reject`
- [ ] GET `/api/evaluation/{evalId}`
- [ ] GET `/api/evaluation/employee/{empId}/history`
- [ ] POST `/api/evaluation/{evalId}/evidence`
- [ ] PUT `/api/evaluation/evidence/{evidenceId}/verify`

### ✅ **Performance Ranking APIs:**
- [ ] POST `/api/performance-ranking/calculate/{cycleId}`
- [ ] POST `/api/performance-ranking/auto-recommend/{evalId}` ⭐
- [ ] POST `/api/performance-ranking/analyze-weak-kpi/{evalId}` ⭐
- [ ] GET `/api/performance-ranking/cycle/{cycleId}/top?limit=10`
- [ ] GET `/api/performance-ranking/cycle/{cycleId}/promotion-candidates`
- [ ] GET `/api/performance-ranking/employee/{empId}/cycle/{cycleId}`

### ✅ **Training APIs:**
- [ ] POST `/api/training/assign`
- [ ] POST `/api/training/assign/mentor`
- [ ] PUT `/api/training/progress/{progressId}`
- [ ] PUT `/api/training/progress/{progressId}/complete` ⭐
- [ ] POST `/api/training/certificate`
- [ ] PUT `/api/training/certificate/{certId}/verify`
- [ ] GET `/api/training/certificate/pending`

---

## 🛠️ TROUBLESHOOTING

### **Lỗi 1: Không kết nối được database**
```
Error: Cannot connect to database
```

**Giải quyết:**
1. Kiểm tra SQL Server đã chạy chưa
2. Check username/password trong `application.properties`
3. Database `hrm_system_db` đã tạo chưa

### **Lỗi 2: Swagger không hiển thị API**
```
http://localhost:8080/swagger-ui.html → 404
```

**Giải quyết:**
1. Maven reload dependencies: `mvnw clean install`
2. Restart server
3. Thử URL: `http://localhost:8080/swagger-ui/index.html`

### **Lỗi 3: 403 Forbidden khi test API**
```
Response: 403 Forbidden
```

**Giải quyết:**
- Check SecurityConfig đã có `.requestMatchers("/api/**").permitAll()` chưa
- CSRF đã disable chưa: `.csrf(csrf -> csrf.disable())`

### **Lỗi 4: Data không có (null pointer)**
```
Error: Employee not found / KPI not found
```

**Giải quyết:**
- Cần insert seed data trước:
  - Roles, Users, Employees
  - Departments, Job Positions
  - KPI Templates
  - Training Programs
  - Eval Cycles

---

## 📊 EXPORT API DOCUMENTATION

### **Lấy OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```
→ Copy JSON để import vào Postman

### **Lấy OpenAPI YAML:**
```
http://localhost:8080/v3/api-docs.yaml
```

---

## 🎉 KẾT LUẬN

Bây giờ bạn có:
1. ✅ **Swagger UI** - Giao diện web test API
2. ✅ **OpenAPI Documentation** - Tài liệu tự động
3. ✅ **Test Cases** - Kịch bản test đầy đủ

**Next Steps:**
1. Chạy server
2. Mở Swagger UI
3. Test từng API theo test cases
4. Fix bugs nếu có
5. Deploy production!

---

**Happy Testing! 🚀**
