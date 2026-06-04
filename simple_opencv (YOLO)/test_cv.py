import cv2
import numpy as np
from collections import Counter
from ultralytics import YOLO
import os

# Load YOLO model
ob = YOLO('yolov8n.pt')

# Use absolute path
video_path = os.path.join(os.path.dirname(__file__), "super-Kids-video.mp4")
fourcc = cv2.VideoWriter_fourcc(*'XVID') # specify the video codec 4-byte character
#out = cv2.VideoWriter('output.avi', fourcc, 20.0, (640, 480)) # specify the output name, codec, fps, frame size window
output_size = (960, 540) # output size (width, height) --> 1920x1080 (50% spatial resolution)
frames_per_second = 20.0
confidence_thresh = 0.5
print(f"Video path: {video_path}")

cap = cv2.VideoCapture(video_path)

frame_count = 0

# Check if video opened successfully
if not cap.isOpened():
    print(f"Error: Could not open video file '{video_path}'")
    video_path = r"C:\Users\DELL\Desktop\Python\simple_opencv (YOLO)\Traffic.mp4"
    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print(f"Still failed to read the video frame: '{video_path}'")
        exit()

orig_w = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
orig_h = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))  

print(f"Video opened : {cap.isOpened()}")
print(f"Video width  : {orig_w}")
print(f"Video height : {orig_h}")
print(f"Total frames : {total_frames}")
print(f"Output size  : {output_size[0]}x{output_size[1]}")

fourcc = cv2.VideoWriter_fourcc(*'XVID')
out = cv2.VideoWriter('output.avi', fourcc, frames_per_second, output_size)  # output_size matches 

if not out.isOpened():
    print('Error: could not open VideoWriter'); cap.release(); exit()

frame_count    = 0
total_detected = 0

scale_x = output_size[0] / orig_w
scale_y = output_size[1] / orig_h

while True:
    ret, frame = cap.read()
    if not ret:
        break

    frame_count += 1

    if frame is None or frame.size == 0:
        continue

    
    results     = ob(frame, conf=confidence_thresh, verbose=False)
    boxes       = results[0].boxes
    num_detected = len(boxes)
    total_detected += num_detected

    #  resizing first, then draw boxes ON compressed (not on original frame)
    compressed = cv2.resize(frame, output_size)

    class_counts = Counter()
    for box in boxes:  # loop once, removed redundant inner loop
        x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
        cls_id    = int(box.cls[0])
        box_conf  = float(box.conf[0])  # use separate variable, don't overwrite conf_thresh
        label     = ob.names[int(box.cls[0])]
        class_counts[label] += 1

        print(f'frame {frame_count}: {dict(class_counts)}')  # print counts for current frame
        # Scale coords to compressed frame
        cx1 = int(x1 * scale_x)
        cy1 = int(y1 * scale_y)
        cx2 = int(x2 * scale_x)
        cy2 = int(y2 * scale_y)

        cv2.rectangle(compressed, (cx1, cy1), (cx2, cy2), (0, 255, 0), 2)  
        cv2.putText(compressed, f'{label} {box_conf:.2f}',
                    (cx1, cy1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.45, (0, 255, 0), 1)

    # Overlay stats
    cv2.putText(compressed,
                f"Frame: {frame_count}/{total_frames} | Detected: {num_detected}",
                (10, 25), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 200, 255), 2)

    # `compressed` (960x540) — matches VideoWriter size
    out.write(compressed)

    cv2.imshow("Compressed Output", compressed)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

print(f"\rProcessing: {frame_count}/{total_frames} frames "
      f"({frame_count/total_frames*100:.1f}%)", end="")

cap.release()
out.release()
cv2.destroyAllWindows()

# ── Compression report ──────────────────────────────
orig_size_mb = os.path.getsize(video_path) / (1024 * 1024)
comp_size_mb = os.path.getsize('output.avi') / (1024 * 1024)
saved_pct    = (1 - comp_size_mb / orig_size_mb) * 100

print("\n" + "-"*45)
print("        COMPRESSION REPORT")
print()
print(f"  Input  resolution : {orig_w} x {orig_h}")
print(f"  Output resolution : {output_size[0]} x {output_size[1]}")
print(f"  Total frames      : {frame_count}")
print(f"  Total detections  : {total_detected}")
print(f"  Avg detections/fr : {total_detected / max(frame_count, 1):.1f}")
print(f"  Original size     : {orig_size_mb:.2f} MB")
print(f"  Compressed size   : {comp_size_mb:.2f} MB")
print(f"  Space saved       : {saved_pct:.1f}%")
#print("-"*45)