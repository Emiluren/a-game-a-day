(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def SPACE 32)

(def obstacle-time 60)

(def jump-speed -8)

(def px 100)

(def init-state
  {:py 200, :pv jump-speed, :os (), :t obstacle-time})

(defonce game-state (atom init-state))

(defn set-v! [k v]
  (swap! game-state assoc k v))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= SPACE kc) (set-v! :pv jump-speed))))

(.addEventListener js/document "keydown" on-key-down)

(defn random-height []
  (.floor js/Math (+ 20 (* (.random js/Math) 200))))

(defn update-state [{:keys [py pv t] :as state}]
  (let []
    (-> state
        (update :pv (partial + 0.5))
        (update :t dec)
        (update :py (partial + pv))
        (update :os (partial map (fn [[x y]] [(- x 3) y])))
        (update :os (partial filter (fn [[x _]] (> x -20))))
        (cond-> (< t 0) (-> (assoc :t obstacle-time)
                            (update :os (partial cons [400 (random-height)])))))))

(defn draw-state [{:keys [py os]}]
  (set! (.-fillStyle ctx) "black")
  (.fillRect ctx 0 0 400 400)

  (set! (.-fillStyle ctx) "grey")
  (doseq [[ox oy] os]
    (.fillRect ctx ox 0 20 oy)
    (.fillRect ctx ox (+ oy 80) 20 400))

  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx px py 20 20))

(defn obstacle-coll [{:keys [py os]}]
  (.log js/console (str os))
  (some (fn [[ox oy]]
          (and (> (+ px 20) ox)
               (> (+ ox 20) px)
               (or (< py oy)
                   (> (+ py 20) (+ oy 80)))))
        os))

(defn game-loop []
  (swap! game-state update-state)
  (when (or (> (:py @game-state) 400) (obstacle-coll @game-state))
    (reset! game-state init-state))
  (draw-state @game-state))

(defonce interval-id (js/setInterval game-loop (/ 1000 60)))
