package com.attendx.config;

import com.attendx.model.*;
import com.attendx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with all data from mockData.js on first startup.
 * Skips seeding if users already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository       userRepo;
    @Autowired private SubjectRepository    subjectRepo;
    @Autowired private DepartmentRepository deptRepo;
    @Autowired private TimetableSlotRepository ttRepo;
    @Autowired private PasswordEncoder      encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            System.out.println("[AttendX] Database already seeded, skipping.");
            return;
        }
        System.out.println("[AttendX] Seeding database...");
        seedUsers();
        seedSubjects();
        seedDepartments();
        seedTimetable();
        System.out.println("[AttendX] Seeding complete.");
    }

    // ── USERS ────────────────────────────────────────────────────────────────
    private void seedUsers() {
        List<User> users = List.of(
            // Admin
            u("a1","Dr. Rajesh Kumar",   "admin@college.edu",  "admin123",   "admin",   "Computer Engineering","RK", null,        null,     false),
            // Faculty
            u("f1","Prof. Anita Sharma", "anita@college.edu",  "faculty123", "faculty", "Computer Engineering","AS", null,        null,     false),
            u("f2","Prof. Vikram Nair",  "vikram@college.edu", "faculty123", "faculty", "Computer Engineering","VN", null,        null,     false),
            u("f3","Prof. Priya Menon",  "priya@college.edu",  "faculty123", "faculty", "Computer Engineering","PM", null,        null,     false),
            u("f4","Prof. Suresh Patel", "suresh@college.edu", "faculty123", "faculty", "Computer Engineering","SP", null,        null,     false),
            u("f5","Prof. Meera Joshi",  "meera@college.edu",  "faculty123", "faculty", "Computer Engineering","MJ", null,        null,     false),
            u("f6","Prof. Rahul Desai",  "rahul@college.edu",  "faculty123", "faculty", "Computer Engineering","RD", null,        null,     false),
            u("f7","Prof. Sneha Iyer",   "sneha@college.edu",  "faculty123", "faculty", "Computer Engineering","SI", null,        null,     false),
            // Batch A1
            u("s1",  "Aanchal Mishra",              "aanchal@student.edu",      "student123","student","Computer Engineering","AM","24CE1001","CE-A1",false),
            u("s2",  "Aaron Pravin Henry",           "aaron@student.edu",        "student123","student","Computer Engineering","AP","24CE1002","CE-A1",false),
            u("s3",  "Aayan Azeem Kanjiani",         "aayan@student.edu",        "student123","student","Computer Engineering","AA","24CE1003","CE-A1",false),
            u("s4",  "Aayush Shibu",                 "aayush@student.edu",       "student123","student","Computer Engineering","AS","24CE1004","CE-A1",false),
            u("s5",  "Varun Sanjay Adagale",         "varun@student.edu",        "student123","student","Computer Engineering","VA","24CE1005","CE-A1",false),
            u("s6",  "Adhav Parth Ashok",            "adhav@student.edu",        "student123","student","Computer Engineering","AP","24CE1006","CE-A1",false),
            u("s7",  "Aditi Ashish Gandre",          "aditig@student.edu",       "student123","student","Computer Engineering","AG","24CE1007","CE-A1",false),
            u("s8",  "Aditi Pintu Rai",              "aditir@student.edu",       "student123","student","Computer Engineering","AR","24CE1008","CE-A1",false),
            u("s9",  "Aditi Pradeep Bhagat",         "aditib@student.edu",       "student123","student","Computer Engineering","AB","24CE1009","CE-A1",false),
            u("s10", "Aditi Vaibhav Bhosle",         "aditibh@student.edu",      "student123","student","Computer Engineering","AB","24CE1010","CE-A1",false),
            u("s11", "Aditya Pandit",                "adityap@student.edu",      "student123","student","Computer Engineering","AP","24CE1011","CE-A1",false),
            u("s12", "Aditya Sachin Sonavane",       "adityas@student.edu",      "student123","student","Computer Engineering","AS","24CE1012","CE-A1",true),
            u("s13", "Durva Alshi",                  "durva@student.edu",        "student123","student","Computer Engineering","DA","24CE1013","CE-A1",false),
            u("s14", "Anahita Vinay Bhatnagar",      "anahita@student.edu",      "student123","student","Computer Engineering","AB","24CE1014","CE-A1",false),
            u("s15", "Anunay Singh Bagri",           "anunay@student.edu",       "student123","student","Computer Engineering","AB","24CE1015","CE-A1",false),
            u("s16", "Anurag Govind Sharma",         "anurag@student.edu",       "student123","student","Computer Engineering","AG","24CE1016","CE-A1",false),
            u("s17", "Anusha Singh",                 "anusha@student.edu",       "student123","student","Computer Engineering","AS","24CE1017","CE-A1",false),
            u("s18", "Arjun Asthana",                "arjun@student.edu",        "student123","student","Computer Engineering","AA","24CE1018","CE-A1",false),
            u("s19", "Arnav Ajay Mishra",            "arnav@student.edu",        "student123","student","Computer Engineering","AM","24CE1019","CE-A1",true),
            u("s20", "Arpit Prakash Singh",          "arpit@student.edu",        "student123","student","Computer Engineering","AP","24CE1020","CE-A1",false),
            // Batch A2
            u("s21", "Aryaman Kudada",               "aryaman@student.edu",      "student123","student","Computer Engineering","AK","24CE1021","CE-A2",false),
            u("s22", "Ashwin Sundram",               "ashwin@student.edu",       "student123","student","Computer Engineering","AS","24CE1024","CE-A2",false),
            u("s23", "Atharv Kiran Bhoir",           "atharvk@student.edu",      "student123","student","Computer Engineering","AB","24CE1025","CE-A2",false),
            u("s24", "Atharva Sanjay Sawant",        "atharvs@student.edu",      "student123","student","Computer Engineering","AS","24CE1026","CE-A2",false),
            u("s25", "Atharva Avhad",                "atharvaa@student.edu",     "student123","student","Computer Engineering","AA","24CE1027","CE-A2",true),
            u("s26", "Amey Mangesh Bagwe",           "amey@student.edu",         "student123","student","Computer Engineering","AM","24CE1028","CE-A2",false),
            u("s27", "Amruta Barde",                 "amruta@student.edu",       "student123","student","Computer Engineering","AB","24CE1029","CE-A2",false),
            u("s28", "Tarandip Singh Basson",        "tarandip@student.edu",     "student123","student","Computer Engineering","TS","24CE1030","CE-A2",false),
            u("s29", "Arya Nitin Bhagwat",           "arya@student.edu",         "student123","student","Computer Engineering","AB","24CE1031","CE-A2",false),
            u("s30", "Bhakti Tulshiram Gadge",       "bhakti@student.edu",       "student123","student","Computer Engineering","BG","24CE1032","CE-A2",false),
            u("s31", "Bhatt Nandini Nileshkumar",    "nandini@student.edu",      "student123","student","Computer Engineering","BN","24CE1034","CE-A2",false),
            u("s32", "Shubham Sudhir Bhoir",         "shubham@student.edu",      "student123","student","Computer Engineering","SB","24CE1035","CE-A2",false),
            u("s33", "Bhupali Prashant Patil",       "bhupali@student.edu",      "student123","student","Computer Engineering","BP","24CE1037","CE-A2",false),
            u("s34", "Diksha Bhuyan",                "diksha@student.edu",       "student123","student","Computer Engineering","DB","24CE1038","CE-A2",true),
            u("s35", "Adeeb Fahim Bijle",            "adeeb@student.edu",        "student123","student","Computer Engineering","AB","24CE1039","CE-A2",false),
            u("s36", "Rishi Chandel",                "rishi@student.edu",        "student123","student","Computer Engineering","RC","24CE1040","CE-A2",false),
            u("s37", "Ishan Vijay Chaubey",          "ishan@student.edu",        "student123","student","Computer Engineering","IC","24CE1041","CE-A2",false),
            u("s38", "Chaurasiya Krish Omprakash",   "krish@student.edu",        "student123","student","Computer Engineering","CK","24CE1042","CE-A2",false),
            // Batch A3
            u("s39", "Chinmay Vinayak Cheulkar",     "chinmay@student.edu",      "student123","student","Computer Engineering","CC","24CE1043","CE-A3",false),
            u("s40", "Anish Hemraj Chitnis",         "anish@student.edu",        "student123","student","Computer Engineering","AC","24CE1044","CE-A3",false),
            u("s41", "Aaryan Karan Choube",          "aaryan@student.edu",       "student123","student","Computer Engineering","AC","24CE1045","CE-A3",false),
            u("s42", "Sanika Rangnath Choudhari",    "sanika@student.edu",       "student123","student","Computer Engineering","SC","24CE1046","CE-A3",false),
            u("s43", "Somansh Rakesh Dafade",        "somansh@student.edu",      "student123","student","Computer Engineering","SD","24CE1047","CE-A3",false),
            u("s44", "Saurav Uttamrao Deore",        "saurav@student.edu",       "student123","student","Computer Engineering","SD","24CE1050","CE-A3",true),
            u("s45", "Aryesh Aniket Deshmukh",       "aryesh@student.edu",       "student123","student","Computer Engineering","AD","24CE1051","CE-A3",false),
            u("s46", "Devashish Bobby",              "devashish@student.edu",    "student123","student","Computer Engineering","DB","24CE1052","CE-A3",false),
            u("s47", "Devendranath Ravinath Tiwari", "devendranath@student.edu", "student123","student","Computer Engineering","DT","24CE1053","CE-A3",false),
            u("s48", "Devireddy Bharadwaja Reddy",   "devireddy@student.edu",    "student123","student","Computer Engineering","DR","24CE1054","CE-A3",false),
            u("s49", "Dharmi Jain",                  "dharmi@student.edu",       "student123","student","Computer Engineering","DJ","24CE1055","CE-A3",false),
            u("s50", "Dhruvi Dinesh Hegde",          "dhruvi@student.edu",       "student123","student","Computer Engineering","DH","24CE1056","CE-A3",false),
            u("s51", "Sherwin Dsouza",               "sherwin@student.edu",      "student123","student","Computer Engineering","SD","24CE1057","CE-A3",false),
            u("s52", "Atharva Birendranath Dubey",   "atharvabd@student.edu",    "student123","student","Computer Engineering","AD","24CE1058","CE-A3",false),
            u("s53", "Dhruv Ambika Prasad Dubey",    "dhruv@student.edu",        "student123","student","Computer Engineering","DD","24CE1059","CE-A3",false),
            u("s54", "Sujal Narendraprasad Dubey",   "sujal@student.edu",        "student123","student","Computer Engineering","SD","24CE1060","CE-A3",false),
            u("s55", "Eshan Arya",                   "eshan@student.edu",        "student123","student","Computer Engineering","EA","24CE1061","CE-A3",false),
            u("s56", "Chris Augustine Fernandes",    "chris@student.edu",        "student123","student","Computer Engineering","CF","24CE1062","CE-A3",false),
            u("s57", "Gandhi Raj Sandesh",           "gandhi@student.edu",       "student123","student","Computer Engineering","GR","24CE1063","CE-A3",false),
            u("s58", "Gandre Paras Abhay",           "paras@student.edu",        "student123","student","Computer Engineering","GP","24CE1064","CE-A3",false),
            u("s59", "Atharva Narendra Gharat",      "atharvag@student.edu",     "student123","student","Computer Engineering","AG","24CE1065","CE-A3",false)
        );
        userRepo.saveAll(users);
        System.out.println("[AttendX] Seeded " + users.size() + " users.");
    }

    private User u(String id, String name, String email, String pass, String role,
                   String dept, String avatar, String rollNo, String cls, boolean defaulter) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(pass));
        u.setRole(role);
        u.setDepartment(dept);
        u.setAvatar(avatar);
        u.setRollNo(rollNo);
        u.setClassName(cls);
        u.setDefaulter(defaulter);
        return u;
    }

    // ── SUBJECTS ─────────────────────────────────────────────────────────────
    private void seedSubjects() {
        List<Subject> subjects = List.of(
            sub("IOT","Internet of Things (IOT)",      "Computer Engineering","CE-ALL","f1"),
            sub("DSE","Data Structures Engg (DSE)",    "Computer Engineering","CE-ALL","f2"),
            sub("TCS","Theory of Computer Sci (TCS)",  "Computer Engineering","CE-ALL","f3"),
            sub("CNS","Computer Networks & Sec (CNS)", "Computer Engineering","CE-ALL","f4"),
            sub("FM", "Financial Management (FM)",     "Computer Engineering","CE-ALL","f5"),
            sub("PM", "Project Management (PM)",       "Computer Engineering","CE-ALL","f6"),
            sub("COI","Constitution of India (COI)",   "Computer Engineering","CE-ALL","f7")
        );
        subjectRepo.saveAll(subjects);
        System.out.println("[AttendX] Seeded " + subjects.size() + " subjects.");
    }

    private Subject sub(String id, String name, String dept, String cls, String fId) {
        Subject s = new Subject();
        s.setId(id); s.setName(name); s.setDepartment(dept); s.setClassName(cls); s.setFacultyId(fId);
        return s;
    }

    // ── DEPARTMENTS ──────────────────────────────────────────────────────────
    private void seedDepartments() {
        Department d = new Department();
        d.setId("d1");
        d.setName("Computer Engineering");
        d.setHod("Dr. Rajesh Kumar");
        d.setStudents(59);
        d.setFaculty(7);
        deptRepo.save(d);
        System.out.println("[AttendX] Seeded 1 department.");
    }

    // ── TIMETABLE ────────────────────────────────────────────────────────────
    private void seedTimetable() {
        List<TimetableSlot> slots = List.of(
            tt("tt1",  "Monday",    "08:00-09:00", "IOT", "CE-ALL", "LH-101", "f1"),
            tt("tt2",  "Monday",    "09:00-10:00", "DSE", "CE-ALL", "LH-101", "f2"),
            tt("tt3",  "Monday",    "10:00-11:00", "TCS", "CE-ALL", "LH-101", "f3"),
            tt("tt4",  "Tuesday",   "08:00-09:00", "CNS", "CE-ALL", "LH-101", "f4"),
            tt("tt5",  "Tuesday",   "09:00-10:00", "FM",  "CE-ALL", "LH-101", "f5"),
            tt("tt6",  "Wednesday", "08:00-09:00", "PM",  "CE-ALL", "LH-101", "f6"),
            tt("tt7",  "Wednesday", "09:00-10:00", "COI", "CE-ALL", "LH-101", "f7"),
            tt("tt8",  "Thursday",  "08:00-09:00", "IOT", "CE-ALL", "LH-101", "f1"),
            tt("tt9",  "Thursday",  "09:00-10:00", "DSE", "CE-ALL", "LH-101", "f2"),
            tt("tt10", "Friday",    "08:00-09:00", "TCS", "CE-ALL", "LH-101", "f3"),
            tt("tt11", "Friday",    "09:00-10:00", "CNS", "CE-ALL", "LH-101", "f4")
        );
        ttRepo.saveAll(slots);
        System.out.println("[AttendX] Seeded " + slots.size() + " timetable slots.");
    }

    private TimetableSlot tt(String id, String day, String time, String subj,
                              String cls, String room, String fId) {
        TimetableSlot t = new TimetableSlot();
        t.setId(id); t.setDay(day); t.setTime(time); t.setSubject(subj);
        t.setClassName(cls); t.setRoom(room); t.setFacultyId(fId);
        return t;
    }
}
