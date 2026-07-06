import React, { useEffect, useState } from 'react';
import { Typography, Grid, Card, CardContent } from '@mui/material';
import { lecturerService } from '../services/lecturerService';

const Home = () => {
  const [lecturers, setLecturers] = useState([]);

  useEffect(() => {
    lecturerService.listPage().then((res) => {
      setLecturers(res.content || []);
    });
  }, []);

  return (
    <div>
      <Typography variant="h4" gutterBottom>
        Home
      </Typography>
      <Grid container spacing={3}>
        {lecturers.map((lecturer) => (
          <Grid item xs={12} sm={6} md={4} key={lecturer.id}>
            <Card>
              <CardContent>
                <Typography variant="h6">{lecturer.fullName}</Typography>
                <Typography color="textSecondary">{lecturer.facultyName}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </div>
  );
};

export default Home;
