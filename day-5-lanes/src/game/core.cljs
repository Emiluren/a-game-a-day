(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def LEFT 37)
(def UP 38)
(def RIGHT 39)
(def DOWN 40)

(def screen-width 400)
(def screen-height 400)

(def player-y 300)

(def lane-xs [100 200 300])

(def obstacle-time 60)
(def obstacle-speed 2)

(def init-state
  {:lane 0
   :obstacles ()
   :obstacle-timer obstacle-time})

(defonce game-state (atom init-state))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (swap! game-state update :lane #(max -1 (dec %)))
      (= RIGHT kc) (swap! game-state update :lane #(min 1 (inc %))))))

;; Doesn't seem to be working (increasing amount of event listeners on save)
(defonce evt-listener-id
  (.addEventListener js/document "keydown" on-key-down))

(defn rand-lane []
  (->> (.random js/Math)
       (* 3)
       (.floor js/Math)
       dec))

(defn update-state [{:keys [obstacle-timer obstacles lane] :as state}]
  (-> state
      (update :obstacle-timer dec)
      (update :obstacles (partial map
                                  (fn [[y l]] [(+ y obstacle-speed) l])))
      (update :obstacles (partial filter (fn [[y _]] (< y screen-height))))
      (cond-> (<= obstacle-timer 0) (assoc :obstacle-timer obstacle-time
                                           :obstacles (cons [-10 (rand-lane)] obstacles)))))

(defn cube-lane-x [x]
  (- (get lane-xs (inc x)) 4))

(defn draw [{:keys [lane obstacles]}]
  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx 0 0 screen-width screen-height)

  (set! (.-fillStyle ctx) "rgb(200, 0, 0)")
  (doseq [lx lane-xs]
    (.fillRect ctx lx 0 2 screen-height))
  (doseq [[y l] obstacles]
    (.fillRect ctx (cube-lane-x l) y 10 10))
  (.fillRect ctx (cube-lane-x (:lane @game-state)) player-y 10 10))

(defn main-loop []
  (swap! game-state update-state)
  (let [{:keys [lane obstacles]} @game-state]
    (when (some (fn [[y l]] (and (= l lane)
                                 (> y (- player-y 10))
                                 (< y (+ player-y 10))))
                obstacles)
      (reset! game-state init-state)))
  (draw @game-state))

(defonce interval-id (js/setInterval main-loop (/ 1000 60)))
