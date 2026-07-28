import os
import math
from PIL import Image, ImageDraw, ImageFont

os.makedirs('report/diagrams', exist_ok=True)

def get_font(size=16, bold=False):
    font_names = ['segoeuib.ttf', 'arialbd.ttf'] if bold else ['segoeui.ttf', 'arial.ttf']
    for font_name in font_names:
        try:
            return ImageFont.truetype(font_name, size)
        except:
            pass
    return ImageFont.load_default()

def draw_rounded_rect(draw, xy, fill, outline, width=3, radius=12):
    x1, y1, x2, y2 = xy
    draw.rounded_rectangle([x1, y1, x2, y2], radius=radius, fill=fill, outline=outline, width=int(width))

def draw_arrow(draw, start, end, color='#1E3A8A', width=4, arrow_size=14):
    x1, y1 = start
    x2, y2 = end
    draw.line([x1, y1, x2, y2], fill=color, width=int(width))
    angle = math.atan2(y2 - y1, x2 - x1)
    x3 = x2 - arrow_size * math.cos(angle - math.pi / 6)
    y3 = y2 - arrow_size * math.sin(angle - math.pi / 6)
    x4 = x2 - arrow_size * math.cos(angle + math.pi / 6)
    y4 = y2 - arrow_size * math.sin(angle + math.pi / 6)
    draw.polygon([x2, y2, x3, y3, x4, y4], fill=color)

# ==================== 1. SYSTEM ARCHITECTURE DIAGRAM ====================
def generate_system_architecture():
    W, H = 2400, 1650
    img = Image.new('RGB', (W, H), color='#F8FAFC')
    draw = ImageDraw.Draw(img)

    # Title Banner
    draw_rounded_rect(draw, [60, 40, 2340, 130], fill='#0F172A', outline='#0F172A', radius=14)
    draw.text((W//2, 85), "AI INTERVIEW COACH - SYSTEM ARCHITECTURE", fill='#FFFFFF', font=get_font(30, bold=True), anchor='mm')

    layers = [
        ("1. Presentation Layer (Android UI)", "#EFF6FF", "#2563EB", "#1E40AF", 170, 240, [
            ("Question Select Fragment", "Browse HR, Technical &\nBehavioral Question Bank"),
            ("Recorder Preview Fragment", "CameraX Live View,\nTimer & Audio Signal"),
            ("Feedback Report Fragment", "Consolidated Scores,\nMetrics & Actionable Advice"),
            ("History View Fragment", "Local Session History &\nProgress Trend Charts")
        ]),
        ("2. Media Stream Layer", "#F0FDF4", "#16A34A", "#15803D", 450, 220, [
            ("CameraX Video API", "60 FPS Video Preview &\nFrame Capture Stream"),
            ("Audio Stream Extractor", "Concurrent Microphone PCM\nAudio Stream Extractor")
        ]),
        ("3. Multimodal Analytics & ML Engine", "#FEF3C7", "#D97706", "#B45309", 710, 260, [
            ("Android SpeechRecognizer", "Native On-Device Speech-to-Text\nTranscription & Word Timestamps"),
            ("Filler & Pace Analyzer", "WPM Calculation & Filler Word\nDetection ('um', 'like', 'uh')"),
            ("Google ML Kit Vision", "Face Bounding Box & Pose\nLandmarker Tracking"),
            ("Eye Contact Calculator", "Eye Gaze Vector Estimation &\nAlignment Ratio Metric")
        ]),
        ("4. Scoring & Evaluation Rules Engine", "#FEE2E2", "#EF4444", "#B91C1C", 1010, 220, [
            ("Multi-Metric Aggregator", "Normalizes Speech Rate, Filler Penalty,\nEye Contact Ratio & Keyword Relevance"),
            ("Consolidated Score Index", "Generates Final Overall Candidate\nPerformance Index (0 - 100 Scale)")
        ]),
        ("5. Persistence & Data Layer", "#F3E8FF", "#9333EA", "#7E22CE", 1270, 220, [
            ("Room Database (SQLite)", "Structured Storage of Local Sessions,\nScores & Transcripts"),
            ("Firebase Firestore Sync", "Optional Cloud Synchronization for\nAnalytics & Backup")
        ])
    ]

    f_layer_head = get_font(22, bold=True)
    f_box_head = get_font(17, bold=True)
    f_box_sub = get_font(13, bold=False)

    for l_name, bg_col, border_col, header_col, y_start, h_len, boxes in layers:
        # Layer outer container
        draw_rounded_rect(draw, [60, y_start, 2340, y_start + h_len], fill=bg_col, outline=border_col, width=3, radius=14)
        
        # Layer Header Banner Inside
        draw_rounded_rect(draw, [80, y_start + 15, 750, y_start + 65], fill=header_col, outline=header_col, radius=8)
        draw.text((100, y_start + 40), l_name, fill='#FFFFFF', font=f_layer_head, anchor='lm')

        # Draw boxes inside layer
        n_boxes = len(boxes)
        avail_w = 2200 - (n_boxes - 1) * 30
        box_w = avail_w // n_boxes
        box_y = y_start + 80
        box_h = h_len - 95

        for b_idx, (b_title, b_sub) in enumerate(boxes):
            bx1 = 100 + b_idx * (box_w + 30)
            bx2 = bx1 + box_w
            
            # Card background with top accent line
            draw_rounded_rect(draw, [bx1, box_y, bx2, box_y + box_h], fill='#FFFFFF', outline=border_col, width=2, radius=10)
            draw_rounded_rect(draw, [bx1, box_y, bx2, box_y + 12], fill=header_col, outline=header_col, radius=6)
            
            draw.text(((bx1+bx2)//2, box_y + 35), b_title, fill='#0F172A', font=f_box_head, anchor='mm')
            
            # Draw multi-line subtext
            sub_lines = b_sub.split('\n')
            for line_idx, line in enumerate(sub_lines):
                draw.text(((bx1+bx2)//2, box_y + 65 + line_idx * 22), line, fill='#475569', font=f_box_sub, anchor='mm')

    # Draw Connecting Arrows between Layers
    for i in range(len(layers) - 1):
        y1 = layers[i][4] + layers[i][5]
        y2 = layers[i+1][4]
        draw_arrow(draw, (1200, y1), (1200, y2), color='#334155', width=5, arrow_size=16)

    # Caption
    draw.text((W//2, H - 35), "Fig 1. System Architecture Diagram - AI Interview Coach", fill='#334155', font=get_font(16, bold=True), anchor='mm')

    img.save('report/diagrams/system_architecture.png')
    print("Saved high-res system_architecture.png")

# ==================== 2. USE CASE DIAGRAM ====================
def generate_use_case_diagram():
    W, H = 2400, 1650
    img = Image.new('RGB', (W, H), color='#FFFFFF')
    draw = ImageDraw.Draw(img)

    # Title
    draw_rounded_rect(draw, [60, 40, 2340, 130], fill='#0F172A', outline='#0F172A', radius=14)
    draw.text((W//2, 85), "AI INTERVIEW COACH - USE CASE DIAGRAM", fill='#FFFFFF', font=get_font(30, bold=True), anchor='mm')

    # System Boundary Box
    draw_rounded_rect(draw, [480, 160, 1920, 1530], fill='#F8FAFC', outline='#1E293B', width=4, radius=16)
    draw.text((510, 195), "System Boundary: AI Interview Coach Application", fill='#1E293B', font=get_font(22, bold=True))

    # Actors
    def draw_stick_figure(x, y, name):
        draw.ellipse([x-40, y-90, x+40, y-10], outline='#1E3A8A', fill='#EFF6FF', width=4) # Head
        draw.line([x, y-10, x, y+100], fill='#1E3A8A', width=5) # Body
        draw.line([x-65, y+30, x+65, y+30], fill='#1E3A8A', width=5) # Arms
        draw.line([x, y+100, x-45, y+190], fill='#1E3A8A', width=5) # Left Leg
        draw.line([x, y+100, x+45, y+190], fill='#1E3A8A', width=5) # Right Leg
        
        # Name Box
        draw_rounded_rect(draw, [x-130, y+210, x+130, y+275], fill='#1E3A8A', outline='#1E3A8A', radius=8)
        draw.text((x, y+242), name, fill='#FFFFFF', font=get_font(18, bold=True), anchor='mm')

    draw_stick_figure(240, 750, "Student /\nCandidate")
    draw_stick_figure(2160, 750, "Firebase /\nLocal Storage")

    # Use Cases (Large formatted Ovals inside system boundary)
    use_cases = [
        ("UC1", 1200, 280, "UC1: Select Question Category & Difficulty Level", 290, 55),
        ("UC2", 1200, 440, "UC2: Record Mock Interview (CameraX Video & Audio)", 310, 58),
        ("UC3", 950, 640, "UC3: Transcribe Speech & Count Filler Words", 280, 55),
        ("UC4", 1450, 640, "UC4: Analyze Eye Contact & Body Posture", 280, 55),
        ("UC5", 1200, 860, "UC5: Compute Score Index & Generate Feedback Report", 320, 62),
        ("UC6", 1200, 1080, "UC6: View Performance History & Visual Progress Trends", 310, 58),
        ("UC7", 1200, 1300, "UC7: Persist & Synchronize Session Metrics", 280, 55)
    ]

    f_uc = get_font(16, bold=True)
    uc_coords = {}
    for uc_id, cx, cy, text, rx, ry in use_cases:
        draw.ellipse([cx - rx, cy - ry, cx + rx, cy + ry], fill='#EFF6FF', outline='#2563EB', width=3)
        draw.text((cx, cy), text, fill='#0F172A', font=f_uc, anchor='mm')
        uc_coords[uc_id] = (cx, cy, rx, ry)

    # Candidate Connections
    for uc_id in ["UC1", "UC2", "UC5", "UC6"]:
        cx, cy, rx, ry = uc_coords[uc_id]
        draw.line([370, 750, cx - rx + 15, cy], fill='#1E3A8A', width=3)

    # Database Connections
    cx, cy, rx, ry = uc_coords["UC7"]
    draw.line([2030, 750, cx + rx - 15, cy], fill='#1E3A8A', width=3)

    # Includes & Extends
    f_inc = get_font(15, bold=True)
    # UC2 -> UC3
    draw_arrow(draw, (1100, 485), (990, 585), color='#D97706', width=3, arrow_size=12)
    draw.text((1000, 520), "<<include>>", fill='#D97706', font=f_inc)

    # UC2 -> UC4
    draw_arrow(draw, (1300, 485), (1410, 585), color='#D97706', width=3, arrow_size=12)
    draw.text((1380, 520), "<<include>>", fill='#D97706', font=f_inc)

    # UC3 -> UC5
    draw_arrow(draw, (990, 695), (1110, 800), color='#2563EB', width=3, arrow_size=12)

    # UC4 -> UC5
    draw_arrow(draw, (1410, 695), (1290, 800), color='#2563EB', width=3, arrow_size=12)

    # UC5 -> UC7
    draw_arrow(draw, (1200, 922), (1200, 1242), color='#9333EA', width=4, arrow_size=14)
    draw.text((1220, 1180), "<<include>>", fill='#9333EA', font=f_inc)

    draw.text((W//2, H - 35), "Fig 2. Use Case Diagram - System Actors & Interactions", fill='#334155', font=get_font(16, bold=True), anchor='mm')

    img.save('report/diagrams/use_case_diagram.png')
    print("Saved high-res use_case_diagram.png")

# ==================== 3. DFD LEVEL 0 (CONTEXT DIAGRAM) ====================
def generate_dfd_level_0():
    W, H = 2400, 1400
    img = Image.new('RGB', (W, H), color='#FFFFFF')
    draw = ImageDraw.Draw(img)

    # Title
    draw_rounded_rect(draw, [60, 40, 2340, 130], fill='#0F172A', outline='#0F172A', radius=14)
    draw.text((W//2, 85), "DATA FLOW DIAGRAM - LEVEL 0 (CONTEXT DIAGRAM)", fill='#FFFFFF', font=get_font(30, bold=True), anchor='mm')

    # External Entity 1: Student
    draw_rounded_rect(draw, [120, 520, 520, 820], fill='#EFF6FF', outline='#1D4ED8', width=4, radius=12)
    draw_rounded_rect(draw, [120, 520, 520, 580], fill='#1D4ED8', outline='#1D4ED8', radius=10)
    draw.text((320, 550), "EXTERNAL ENTITY", fill='#FFFFFF', font=get_font(16, bold=True), anchor='mm')
    draw.text((320, 680), "STUDENT /\nCANDIDATE", fill='#0F172A', font=get_font(24, bold=True), anchor='mm')

    # Central Process 0: AI Interview Coach System
    draw.ellipse([950, 450, 1450, 950], fill='#FEF3C7', outline='#D97706', width=5)
    draw.text((1200, 700), "0.0\nAI Interview Coach\nApplication System", fill='#0F172A', font=get_font(25, bold=True), anchor='mm')

    # Data Store D1: System Database
    draw_rounded_rect(draw, [1880, 550, 2280, 790], fill='#F0FDF4', outline='#15803D', width=4, radius=12)
    draw_rounded_rect(draw, [1880, 550, 2280, 610], fill='#15803D', outline='#15803D', radius=10)
    draw.text((2080, 580), "DATA STORE D1", fill='#FFFFFF', font=get_font(16, bold=True), anchor='mm')
    draw.text((2080, 700), "System Database\n(Questions & Sessions)", fill='#0F172A', font=get_font(20, bold=True), anchor='mm')

    # Data Flows
    f_flow = get_font(16, bold=True)
    # Student -> System
    draw_arrow(draw, (520, 620), (950, 620), color='#1D4ED8', width=5, arrow_size=16)
    draw.text((735, 585), "1. Selected Category, Camera & Audio Stream", fill='#1D4ED8', font=f_flow, anchor='mm')

    # System -> Student
    draw_arrow(draw, (950, 780), (520, 780), color='#059669', width=5, arrow_size=16)
    draw.text((735, 815), "2. Consolidated Feedback Report & Scores", fill='#059669', font=f_flow, anchor='mm')

    # System -> DB
    draw_arrow(draw, (1450, 620), (1880, 620), color='#D97706', width=5, arrow_size=16)
    draw.text((1665, 585), "Fetch Question Bank", fill='#D97706', font=f_flow, anchor='mm')

    # DB -> System
    draw_arrow(draw, (1880, 740), (1450, 740), color='#7C3AED', width=5, arrow_size=16)
    draw.text((1665, 775), "Write Session Logs & Transcripts", fill='#7C3AED', font=f_flow, anchor='mm')

    draw.text((W//2, H - 35), "Fig 3. Data Flow Diagram Level 0 (Context Diagram)", fill='#334155', font=get_font(16, bold=True), anchor='mm')

    img.save('report/diagrams/dfd_level_0.png')
    print("Saved high-res dfd_level_0.png")

# ==================== 4. DFD LEVEL 1 (DECOMPOSED PROCESS VIEW) ====================
def generate_dfd_level_1():
    W, H = 2400, 1700
    img = Image.new('RGB', (W, H), color='#FFFFFF')
    draw = ImageDraw.Draw(img)

    # Title
    draw_rounded_rect(draw, [60, 40, 2340, 130], fill='#0F172A', outline='#0F172A', radius=14)
    draw.text((W//2, 85), "DATA FLOW DIAGRAM - LEVEL 1 (DECOMPOSED PROCESS VIEW)", fill='#FFFFFF', font=get_font(30, bold=True), anchor='mm')

    # External Entity & Data Stores
    draw_rounded_rect(draw, [80, 280, 380, 460], fill='#EFF6FF', outline='#1D4ED8', width=4, radius=10)
    draw_rounded_rect(draw, [80, 280, 380, 330], fill='#1D4ED8', outline='#1D4ED8', radius=8)
    draw.text((230, 305), "EXTERNAL ENTITY", fill='#FFFFFF', font=get_font(15, bold=True), anchor='mm')
    draw.text((230, 395), "STUDENT /\nCANDIDATE", fill='#0F172A', font=get_font(20, bold=True), anchor='mm')

    draw_rounded_rect(draw, [80, 750, 380, 910], fill='#F0FDF4', outline='#15803D', width=3, radius=10)
    draw.text((230, 830), "D1 | Question Bank", fill='#15803D', font=get_font(20, bold=True), anchor='mm')

    draw_rounded_rect(draw, [80, 1250, 380, 1410], fill='#F3E8FF', outline='#7C3AED', width=3, radius=10)
    draw.text((230, 1330), "D2 | Session History DB", fill='#7C3AED', font=get_font(20, bold=True), anchor='mm')

    processes = [
        ("1.0 Question\nSelection Engine", 680, 370),
        ("2.0 Media Capture &\nStream Recorder", 680, 830),
        ("3.0 Speech-to-Text &\nFiller Analyzer", 1350, 620),
        ("4.0 Vision Eye Contact\n& Posture Engine", 1350, 1040),
        ("5.0 Score Aggregator\n& Report Generator", 1980, 830),
        ("6.0 Session History\nLogger", 1350, 1330)
    ]

    f_p = get_font(18, bold=True)
    for title, cx, cy in processes:
        draw.ellipse([cx - 150, cy - 95, cx + 150, cy + 95], fill='#FEF3C7', outline='#D97706', width=4)
        draw.text((cx, cy), title, fill='#0F172A', font=f_p, anchor='mm')

    f_fl = get_font(14, bold=True)
    # Student -> 1.0
    draw_arrow(draw, (380, 370), (530, 370), color='#1D4ED8', width=4, arrow_size=14)
    draw.text((455, 340), "Select Category", fill='#1D4ED8', font=f_fl, anchor='mm')

    # D1 -> 1.0
    draw_arrow(draw, (230, 750), (590, 445), color='#15803D', width=4, arrow_size=14)
    draw.text((410, 610), "Read Question List", fill='#15803D', font=f_fl, anchor='mm')

    # 1.0 -> Student
    draw_arrow(draw, (550, 310), (330, 280), color='#1D4ED8', width=4, arrow_size=14)
    draw.text((440, 275), "Display Selected Question", fill='#1D4ED8', font=f_fl, anchor='mm')

    # Student -> 2.0
    draw_arrow(draw, (280, 460), (570, 750), color='#1D4ED8', width=4, arrow_size=14)
    draw.text((370, 630), "Start Record (AV Stream)", fill='#1D4ED8', font=f_fl, anchor='mm')

    # 2.0 -> 3.0
    draw_arrow(draw, (820, 770), (1200, 660), color='#D97706', width=4, arrow_size=14)
    draw.text((1010, 690), "Raw PCM Audio Stream", fill='#D97706', font=f_fl, anchor='mm')

    # 2.0 -> 4.0
    draw_arrow(draw, (820, 890), (1200, 1000), color='#D97706', width=4, arrow_size=14)
    draw.text((1010, 970), "Video Frame Stream", fill='#D97706', font=f_fl, anchor='mm')

    # 3.0 -> 5.0
    draw_arrow(draw, (1490, 650), (1840, 770), color='#059669', width=4, arrow_size=14)
    draw.text((1665, 680), "Transcripts, WPM & Filler Count", fill='#059669', font=f_fl, anchor='mm')

    # 4.0 -> 5.0
    draw_arrow(draw, (1490, 1010), (1840, 890), color='#059669', width=4, arrow_size=14)
    draw.text((1665, 980), "Eye Contact % & Posture Score", fill='#059669', font=f_fl, anchor='mm')

    # 5.0 -> Student
    draw_arrow(draw, (1980, 735), (280, 280), color='#2563EB', width=4, arrow_size=14)
    draw.text((1130, 220), "Generate & Render Consolidated Feedback Report", fill='#2563EB', font=get_font(17, bold=True), anchor='mm')

    # 5.0 -> 6.0
    draw_arrow(draw, (1980, 925), (1490, 1270), color='#7C3AED', width=4, arrow_size=14)
    draw.text((1780, 1130), "Session Summary Object", fill='#7C3AED', font=f_fl, anchor='mm')

    # 6.0 -> D2
    draw_arrow(draw, (1200, 1330), (380, 1330), color='#7C3AED', width=4, arrow_size=14)
    draw.text((790, 1295), "Store Session Performance Record", fill='#7C3AED', font=f_fl, anchor='mm')

    draw.text((W//2, H - 35), "Fig 4. Data Flow Diagram Level 1 (Decomposed Process View)", fill='#334155', font=get_font(16, bold=True), anchor='mm')

    img.save('report/diagrams/dfd_level_1.png')
    print("Saved high-res dfd_level_1.png")

# ==================== 5. TECH STACK DIAGRAM ====================
def generate_tech_stack():
    W, H = 2400, 1650
    img = Image.new('RGB', (W, H), color='#F8FAFC')
    draw = ImageDraw.Draw(img)

    # Title Banner
    draw_rounded_rect(draw, [60, 40, 2340, 130], fill='#0F172A', outline='#0F172A', radius=14)
    draw.text((W//2, 85), "AI INTERVIEW COACH - TECHNOLOGY STACK ARCHITECTURE", fill='#FFFFFF', font=get_font(30, bold=True), anchor='mm')

    stacks = [
        ("Layer 1: Mobile UI & Presentation Framework", "#EFF6FF", "#2563EB", "#1E40AF", 170, [
            ("Android SDK (Kotlin)", "Native Mobile Application Logic &\nTarget API Level 34+ Lifecycle"),
            ("Jetpack Architecture", "MVVM Pattern, ViewBinding, ViewModel\n& LiveData Reactive Architecture"),
            ("Material Design 3", "Modern Responsive Component\nStyling & User Experience")
        ]),
        ("Layer 2: Media Capture & Audio/Video Streaming", "#F0FDF4", "#16A34A", "#15803D", 460, [
            ("CameraX API", "High-performance camera preview &\nconcurrent video recording stream"),
            ("Audio Extractor", "PCM Audio stream extraction from\nmicrophone hardware input")
        ]),
        ("Layer 3: On-Device AI & Analytics Engine", "#FEF3C7", "#D97706", "#B45309", 750, [
            ("Android SpeechRecognizer", "Native Speech-to-Text STT transcription\n& word timestamping"),
            ("Google ML Kit Vision", "Face Landmarker & Eye Gaze Vector\nalignment calculation"),
            ("MediaPipe Framework", "Body Posture, Head Tilt & Pose\nalignment tracking")
        ]),
        ("Layer 4: Data Storage & Cloud Synchronization", "#F3E8FF", "#9333EA", "#7E22CE", 1040, [
            ("Room Database (SQLite)", "Local relational storage for session\nhistory, scores & transcripts"),
            ("Firebase Firestore", "Cloud NoSQL database for optional\nremote backup & analytics sync")
        ]),
        ("Layer 5: Developer Tools & Build Infrastructure", "#F1F5F9", "#475569", "#334155", 1330, [
            ("Android Studio Ladybug", "Primary IDE & Android Emulator\ndebugging platform"),
            ("Git & GitHub", "Source version control, branch workflows\n& collaborative development"),
            ("Gradle Build System", "Automated dependency management\n& APK compilation pipeline")
        ])
    ]

    f_l = get_font(21, bold=True)
    f_b = get_font(17, bold=True)
    f_s = get_font(13, bold=False)

    for l_title, bg_c, border_c, header_c, y_s, items in stacks:
        h_len = 240
        draw_rounded_rect(draw, [60, y_s, 2340, y_s + h_len], fill=bg_c, outline=border_c, width=3, radius=14)
        
        # Header banner inside
        draw_rounded_rect(draw, [80, y_s + 15, 800, y_s + 65], fill=header_c, outline=header_c, radius=8)
        draw.text((100, y_s + 40), l_title, fill='#FFFFFF', font=f_l, anchor='lm')

        n_items = len(items)
        avail_w = 2200 - (n_items - 1) * 30
        box_w = avail_w // n_items
        by = y_s + 80
        bh = h_len - 95

        for b_idx, (b_t, b_s) in enumerate(items):
            bx1 = 100 + b_idx * (box_w + 30)
            bx2 = bx1 + box_w
            
            draw_rounded_rect(draw, [bx1, by, bx2, by + bh], fill='#FFFFFF', outline=border_c, width=2, radius=10)
            draw_rounded_rect(draw, [bx1, by, bx2, by + 12], fill=header_c, outline=header_c, radius=6)
            
            draw.text(((bx1+bx2)//2, by + 35), b_t, fill='#0F172A', font=f_b, anchor='mm')
            
            sub_lines = b_s.split('\n')
            for line_idx, line in enumerate(sub_lines):
                draw.text(((bx1+bx2)//2, by + 65 + line_idx * 22), line, fill='#475569', font=f_s, anchor='mm')

    draw.text((W//2, H - 35), "Fig 5. Proposed Technology Stack Architecture", fill='#334155', font=get_font(16, bold=True), anchor='mm')

    img.save('report/diagrams/tech_stack.png')
    print("Saved high-res tech_stack.png")

if __name__ == "__main__":
    generate_system_architecture()
    generate_use_case_diagram()
    generate_dfd_level_0()
    generate_dfd_level_1()
    generate_tech_stack()
