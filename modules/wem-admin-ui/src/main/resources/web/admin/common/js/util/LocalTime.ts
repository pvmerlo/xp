module api.util {

    export class LocalTime {

        private hours: number;

        private minutes: number;

        private seconds: number;

        private tzo: number;

        constructor(hours: number, minutes: number, tzo: number, seconds?: number) {
            this.hours = hours;
            this.minutes = minutes;
            this.tzo = tzo;
            if (seconds) {
                this.seconds = seconds;
            }
        }

        getHours(): number {
            return this.hours;
        }

        getMinutes(): number {
            return this.minutes;
        }

        getSeconds(): number {
            return this.seconds;
        }

        getTZO(): number {
            return this.tzo;
        }

        toString(): string {
            if (this.seconds) {

                return "" + this.hours + ":" + this.minutes + ":" + this.seconds;
            }
            else {
                return "" + this.hours + ":" + this.minutes;
            }
        }

        static isValidString(s: string): boolean {
            if (isStringBlank(s)) {
                return false;
            }
            var re = /^[0-2]?\d:[0-5]?\d$/;
            return re.test(s);
        }

        static fromString(s: string): LocalTime {
            if (!LocalTime.isValidString(s)) {
                throw new Error("Cannot parse LocalTime from string: " + s);
            }
            var tzo = api.util.DateHelper.getTZOffset();
            var localTime: string[] = s.split(':');
            var hours = Number(localTime[0]);
            var minutes = Number(localTime[1]);
            return new LocalTime(hours, minutes, tzo);


        }
    }
}