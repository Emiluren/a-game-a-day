(ns game.core)

(defonce canvas (.getElementById js/document "cv"))
(defonce ctx (.getContext canvas "2d"))

(def LEFT 37)
(def UP 38)
(def RIGHT 39)
(def DOWN 40)
(def A 65)
(def D 68)

(def pw 80)
(def bs 5)

(def init-level
  (for [[c y] (map vector
                   ["red" "orange" "yellow" "green" "blue"]
                   (iterate inc 0))
        x (range 10)]
    [(* x 40) (* y 20) c]))

(def init-state
  {:px 160, :blocks init-level
   :pl false, :pr false,
   :bx 190, :by 360,
   :xv bs, :yv (- bs)})

(defonce game-state (atom init-state))

(defn set-v! [k v]
  (swap! game-state assoc k v))

(defn on-key-down [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (set-v! :pl true)
      (= RIGHT kc) (set-v! :pr true))))

(defn on-key-up [evt]
  (let [kc (.-keyCode evt)]
    (cond
      (= LEFT kc) (set-v! :pl false)
      (= RIGHT kc) (set-v! :pr false))))

(.addEventListener js/document "keydown" on-key-down)
(.addEventListener js/document "keyup" on-key-up)

(defn bp-coll [bx px]
  (and (> (+ bx 20) px)
       (< bx (+ px pw))))

(defn not-touching [bx by [x y _]]
  (or (> x (+ bx 20))
      (< (+ x 40) bx)
      (> by (+ y 20))
      (< (+ x 20) by)))

(defn block-bounce [bx by [x y _]]
  (let [dx ]))

(defn update-state [{:keys [pl pr px bx by xv yv blocks] :as state}]
  (let [pv (+ (if pl -10 0) (if pr 10 0))
        coll-block (first (filter
                           (complement
                            (partial not-touching bx by))
                           blocks))]
    (-> state
        (update :px + pv)
        (update :bx + xv)
        (update :by + yv)
        (update :blocks (partial filter (partial not-touching bx by)))
        (cond-> (< bx 0) (assoc :xv bs)
                (> bx 380) (assoc :xv (- bs))
                (< by 0) (assoc :yv bs)
                (and (> by 360)
                     (bp-coll bx px)) (assoc :yv (- bs))
                coll-block (block-bounce bx by coll-block)
                (> by 400) (assoc
                            :bx (+ px 30)
                            :by (:by init-state)
                            :xv (:xv init-state)
                            :yv (:yv init-state))))))

(defn draw-state [{:keys [px bx by blocks]}]
  (set! (.-fillStyle ctx) "black")
  (.fillRect ctx 0 0 400 400)

  (doseq [[x y c] blocks]
    (set! (.-fillStyle ctx) c)
    (.fillRect ctx x y 40 20))

  (set! (.-fillStyle ctx) "grey")
  (.fillRect ctx px 380 pw 20)

  (set! (.-fillStyle ctx) "white")
  (.fillRect ctx bx by 20 20))

(defn game-loop []
  (swap! game-state update-state)
  (draw-state @game-state))

(defonce interval-id (js/setInterval game-loop (/ 1000 60)))
