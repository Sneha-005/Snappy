# Snappy Ruler Set

**Snappy Ruler Set** is an Android drawing app that combines freehand drawing with advanced virtual geometry tools. It allows users to create precise constructions quickly with “snapping” features for angles, endpoints, and intersections.

---

## Features

### Core Tools
- **Ruler**
  - Drag, rotate, and position with two fingers.
  - Draw straight lines along its edge.
  - Magnetic snapping to common angles: 0°, 30°, 45°, 60°, 90°.
  - Snap to existing line endpoints and midpoints when close.

- **Set Square (Triangle)**
  - Two variants: 45° and 30°/60°.
  - Edge-aligned drawing with snapping to canvas grid and existing segments.

- **Protractor**
  - Place over a vertex to measure angles between two rays.
  - Snap readout to nearest 1°.
  - Hard snap at common angles: 30°, 45°, 60°, 90°, 120°, 135°, 150°, 180°.

- **Compass (Optional)**
  - Set radius by dragging.
  - Draw arcs/circles snapping to intersections or points.

---

### Interactions & UX
- **Snappy Feel**
  - Magnetic snapping with subtle visual hints (ticks & highlights) and gentle haptic feedback.
- **Gestures**
  - One-finger drag: Pan canvas.
  - Two-finger pinch: Zoom canvas.
  - Two-finger rotate: Rotate selected tool (if applicable).
  - Long-press: Temporarily toggle snapping on/off.
- **Precision HUD**
  - Small overlay showing angle/length while drawing.
- **Undo/Redo**
  - At least 20 steps of undo/redo.
- **Export**
  - Save drawings as PNG or JPEG with optional sharing.

---

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/snappy-ruler-set.git
