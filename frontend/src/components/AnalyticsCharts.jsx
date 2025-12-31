import React from 'react';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell,
    Legend
} from 'recharts';
import './AnalyticsCharts.css';

function AnalyticsCharts({ stats, events }) {
    // Use actual monthly data from backend
    const barData = stats.monthlyData || [
        { name: 'Jan', events: 0, certs: 0 },
        { name: 'Feb', events: 0, certs: 0 },
        { name: 'Mar', events: 0, certs: 0 },
        { name: 'Apr', events: 0, certs: 0 },
        { name: 'May', events: 0, certs: 0 },
        { name: 'Jun', events: 0, certs: 0 },
        { name: 'Jul', events: 0, certs: 0 },
        { name: 'Aug', events: 0, certs: 0 },
        { name: 'Sep', events: 0, certs: 0 },
        { name: 'Oct', events: 0, certs: 0 },
        { name: 'Nov', events: 0, certs: 0 },
        { name: 'Dec', events: 0, certs: 0 }
    ];

    // Merge actual data into the full year template to ensure all months visible
    const fullYearData = barData; // Assuming backend sends all months or we handle it here

    return (
        <div className="analytics-section single-chart">
            <div className="chart-card activity-card">
                <h4>Monthly Activity</h4>
                <ResponsiveContainer width="100%" height={400}>
                    <BarChart data={barData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                        <XAxis
                            dataKey="name"
                            axisLine={false}
                            tickLine={false}
                            interval={0}
                            tick={{ fontSize: 12 }}
                        />
                        <YAxis axisLine={false} tickLine={false} />
                        <Tooltip
                            contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                        />
                        <Legend />
                        <Bar dataKey="events" fill="#6366f1" radius={[6, 6, 0, 0]} name="Events Uploaded" barSize={30} />
                        <Bar dataKey="certs" fill="#f43f5e" radius={[6, 6, 0, 0]} name="Certificates Sent" barSize={30} />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}

export default AnalyticsCharts;
