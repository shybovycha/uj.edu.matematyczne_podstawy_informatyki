class FieldSolver
    def initialize(field)
        @field = field
        @cars = []
    end

    def detect_cars
        @cars
    end

    def car_x_unblocked?
        @field[5][3] == 'X'
    end

    def moves_available
        <<-COMMENT
        # find all indices possible
        indices = (0..5).to_a.product (0..5).to_a

        # select empty cells' indices
        indices.select! { |pair| x, y = pair; @field[x][y] == -1 }

        puts "FREE INDICES: #{indices.inspect}"

        moves = []

        indices.each do |pair|
            sx, sy = pair

            # GOING RIGHT
            x = sx

            while x < 6 do
                # if we've detected a car
                if @field[sy][x] != -1
                    # and this car lays in our direction
                    if x < 5 and @field[sy][x + 1] == @field[sy][x]
                        # we make it possible for this car to move to our cell
                        1.upto(x - sx) { |dd| moves << { car: @field[sy][x], dx: dd, dy: 0 } }
                        x += 1 while @field[sy][x + 1] == @field[sy][x] and x < 6
                    end
                end

                # in any other case - just skip this cell
                x += 1
            end
        end
        COMMENT

        moves = []



        moves
    end
end

class MainSolver
    def initialize(field)
        @field = field
        @prev_fields = []
    end

    def solve(field, prev_moves = [])
        return nil if @prev_fields.include? field

        solver = FieldSolver.new field

        return prev_moves if solver.car_x_unblocked?

        moves = solver.moves_available

        moves.each do |move|
            puts "MOVE AVAILABLE: #{move.inspect}"
        end
    end

    def solve!
        solve @field
    end
end

field = [
    [-1, -1, -1, -1, -1, -1],
    [-1, -1, -1, -1, -1, -1],
    ['X', 'X', -1, -1, -1, -1],
    [-1, -1, -1, -1, -1, -1],
    [-1, -1, -1, -1, -1, -1],
    [-1, -1, -1, -1, -1, -1],
]

solver = MainSolver.new(field)
solver.solve!